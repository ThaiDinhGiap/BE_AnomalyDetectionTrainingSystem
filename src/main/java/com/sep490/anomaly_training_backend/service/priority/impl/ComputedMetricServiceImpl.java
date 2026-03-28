package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.enums.ComputeMethod;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.service.priority.ComputedMetricService;
import com.sep490.anomaly_training_backend.service.priority.MetricClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ComputedMetricServiceImpl implements ComputedMetricService {

    private final ComputedMetricRepository computedMetricRepository;
    private final PriorityPolicyMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final MetricClassificationService metricClassificationService;

    /**
     * In-memory cache for metric definitions (they rarely change).
     * Key: metricName, Value: ComputedMetric entity
     */
    private final ConcurrentHashMap<String, ComputedMetric> metricCache = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public List<ComputedMetricResponse> getMetricsByEntityType(PolicyEntityType entityType) {
        return mapper.toMetricResponseList(
                computedMetricRepository.findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(entityType)
        );
    }

    @Override
    public Object calculateMetric(Employee employee, String metricName) {
        ComputedMetric metric = getCachedMetric(metricName);
        return calculateMetricValue(employee, metric);
    }

    @Override
    public Map<String, Object> calculateAllMetrics(Employee employee, Set<String> metricNames) {
        Map<String, Object> results = new HashMap<>();

        for (String metricName : metricNames) {
            try {
                Object value = calculateMetric(employee, metricName);
                results.put(metricName, value);
            } catch (Exception e) {
                log.error("Failed to calculate metric: {} for employee: {}", metricName, employee.getId(), e);
                results.put(metricName, null);
            }
        }

        return results;
    }

    /**
     * Batch calculate metrics — optimized to avoid N+1 queries.
     * <p>
     * For SQL metrics: executes a single batch query with IN clause for all employees.
     * For PROPERTY/CLASSIFICATION metrics: evaluated per-employee (already in memory).
     */
    @Override
    public Map<Long, Map<String, Object>> batchCalculateMetrics(List<Employee> employees, Set<String> metricNames) {
        // Initialize result map
        Map<Long, Map<String, Object>> results = new HashMap<>();
        for (Employee emp : employees) {
            results.put(emp.getId(), new HashMap<>());
        }

        List<Long> employeeIds = employees.stream().map(Employee::getId).toList();

        for (String metricName : metricNames) {
            ComputedMetric metric = getCachedMetric(metricName);

            if (metric.getComputeMethod() == ComputeMethod.SQL) {
                // BATCH SQL: Single query for all employees → no N+1
                Map<Long, Object> batchResults = batchCalculateSQL(employeeIds, metric);
                for (Long empId : employeeIds) {
                    results.get(empId).put(metricName, batchResults.getOrDefault(empId, null));
                }
            } else {
                // PROPERTY, CLASSIFICATION, EXTENSION: per-employee (in-memory, no DB hit)
                for (Employee emp : employees) {
                    try {
                        Object value = calculateMetricValue(emp, metric);
                        results.get(emp.getId()).put(metricName, value);
                    } catch (Exception e) {
                        log.error("Failed to calculate metric: {} for employee: {}", metricName, emp.getId(), e);
                        results.get(emp.getId()).put(metricName, null);
                    }
                }
            }
        }

        return results;
    }

    // ── PRIVATE METHODS ─────────────────────────────────────────────────

    /**
     * Get metric definition from cache, loading from DB if not cached.
     */
    private ComputedMetric getCachedMetric(String metricName) {
        return metricCache.computeIfAbsent(metricName, name ->
                computedMetricRepository.findByMetricNameAndDeleteFlagFalse(name)
                        .orElseThrow(() -> new AppException(ErrorCode.METRIC_NOT_FOUND,
                                "Metric not found: " + name))
        );
    }

    /**
     * Force refresh cache (call when metrics are updated via admin API).
     */
    public void evictCache() {
        metricCache.clear();
        log.info("Metric definition cache cleared");
    }

    /**
     * Dispatch metric calculation based on compute method.
     */
    private Object calculateMetricValue(Employee employee, ComputedMetric metric) {
        try {
            return switch (metric.getComputeMethod()) {
                case PROPERTY -> calculateFromProperty(employee, metric.getComputeDefinition());
                case SQL -> calculateFromSQL(employee.getId(), metric.getComputeDefinition(), metric.getReturnType());
                case CLASSIFICATION -> calculateFromClassification(employee, metric.getComputeDefinition());
                case EXTENSION -> calculateFromExtension(employee.getId(), metric.getComputeDefinition());
            };
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating metric: {}", metric.getMetricName(), e);
            throw new AppException(ErrorCode.METRIC_CALCULATION_ERROR,
                    "Failed to calculate metric: " + metric.getMetricName());
        }
    }

    // ── PROPERTY ────────────────────────────────────────────────────────

    /**
     * PROPERTY method: Read a value from an entity using Spring BeanWrapper (reflection).
     * <p>
     * Format: "employee.fieldName" or "employee.methodName()" style.
     * Examples:
     *   - "employee.yearsOfService" → employee.getYearsOfService()
     *   - "employee.onWatchlist"    → employee.isOnWatchlist()
     *   - "employee.status"         → employee.getStatus()
     */
    private Object calculateFromProperty(Employee employee, String propertyPath) {
        String[] parts = propertyPath.split("\\.", 2);
        if (parts.length != 2) {
            throw new AppException(ErrorCode.INVALID_METRIC_DEFINITION,
                    "Invalid property path: " + propertyPath + ". Expected format: 'employee.fieldName'");
        }

        String entityType = parts[0];
        String fieldName = parts[1];

        if (!"employee".equals(entityType)) {
            throw new AppException(ErrorCode.INVALID_METRIC_DEFINITION,
                    "PROPERTY method currently only supports 'employee' entity type, got: " + entityType);
        }

        try {
            BeanWrapper wrapper = new BeanWrapperImpl(employee);
            if (!wrapper.isReadableProperty(fieldName)) {
                throw new AppException(ErrorCode.INVALID_METRIC_DEFINITION,
                        "Property not found on Employee: " + fieldName);
            }
            return wrapper.getPropertyValue(fieldName);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error reading property: {} from Employee: {}", fieldName, employee.getId(), e);
            throw new AppException(ErrorCode.METRIC_CALCULATION_ERROR,
                    "Failed to read property: " + propertyPath);
        }
    }

    // ── SQL (single) ────────────────────────────────────────────────────

    /**
     * SQL method: Execute a parameterized SQL query for a single employee/entity.
     * Uses parameterized queries to prevent SQL injection.
     */
    private Object calculateFromSQL(Long entityId, String sqlTemplate, MetricReturnType returnType) {
        // Replace :entityId with ? placeholder for parameterized query
        String parameterizedQuery = sqlTemplate.replace(":entityId", "?");

        try {
            return switch (returnType) {
                case INT -> jdbcTemplate.queryForObject(parameterizedQuery, Integer.class, entityId);
                case DECIMAL -> jdbcTemplate.queryForObject(parameterizedQuery, BigDecimal.class, entityId);
                case BOOLEAN -> jdbcTemplate.queryForObject(parameterizedQuery, Boolean.class, entityId);
                case STRING -> jdbcTemplate.queryForObject(parameterizedQuery, String.class, entityId);
            };
        } catch (Exception e) {
            log.warn("SQL metric calculation returned null. Template: {}, entityId: {}", sqlTemplate, entityId);
            return null;
        }
    }

    // ── SQL (batch) ─────────────────────────────────────────────────────

    /**
     * Batch SQL: Rewrite single-row SQL metric queries into batch queries.
     * <p>
     * Transforms:
     *   SELECT DATEDIFF(...) FROM ... WHERE employee_id = ?
     * Into:
     *   SELECT employee_id, (DATEDIFF(...)) AS metric_value
     *   FROM employees e
     *   WHERE e.id IN (?, ?, ...)
     * <p>
     * Falls back to per-employee queries if the SQL cannot be batch-transformed.
     */
    private Map<Long, Object> batchCalculateSQL(List<Long> employeeIds, ComputedMetric metric) {
        Map<Long, Object> results = new HashMap<>();

        if (employeeIds.isEmpty()) {
            return results;
        }

        String sqlTemplate = metric.getComputeDefinition();

        // Try to convert single-entity SQL to batch form
        // The SQL format is: SELECT <expr> FROM <source> WHERE <source>.employee_id = :entityId [AND ...]
        // We rewrite it as a correlated subquery per employee
        try {
            String inPlaceholders = employeeIds.stream().map(id -> "?").collect(Collectors.joining(","));

            // Wrap the original query as a correlated subquery
            String batchQuery = "SELECT e.id AS employee_id, (" +
                    sqlTemplate.replace(":entityId", "e.id") +
                    ") AS metric_value FROM employees e WHERE e.id IN (" + inPlaceholders + ") AND e.delete_flag = FALSE";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(batchQuery, employeeIds.toArray());

            for (Map<String, Object> row : rows) {
                Long empId = ((Number) row.get("employee_id")).longValue();
                Object value = castToReturnType(row.get("metric_value"), metric.getReturnType());
                results.put(empId, value);
            }

            log.debug("Batch SQL metric '{}' calculated for {} employees in 1 query",
                    metric.getMetricName(), employeeIds.size());
        } catch (Exception e) {
            // Fallback: per-employee queries
            log.warn("Batch SQL failed for metric '{}', falling back to per-employee queries: {}",
                    metric.getMetricName(), e.getMessage());
            for (Long empId : employeeIds) {
                results.put(empId, calculateFromSQL(empId, sqlTemplate, metric.getReturnType()));
            }
        }

        return results;
    }

    /**
     * Cast a raw DB value to the expected return type.
     */
    private Object castToReturnType(Object value, MetricReturnType returnType) {
        if (value == null) return null;
        return switch (returnType) {
            case INT -> value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
            case DECIMAL -> value instanceof BigDecimal ? value : new BigDecimal(value.toString());
            case BOOLEAN -> value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
            case STRING -> value.toString();
        };
    }

    // ── CLASSIFICATION ──────────────────────────────────────────────────

    /**
     * CLASSIFICATION method: Apply metric classification rules from the
     * metric_classifications table.
     * <p>
     * Steps:
     * 1. Find the source metric name from the classification rules (metric_source)
     * 2. Calculate the source metric value for this employee
     * 3. Apply classification rules to determine the output level
     * <p>
     * The compute_definition stores the classification group name (e.g., "process_classification").
     */
    private Object calculateFromClassification(Employee employee, String classificationName) {
        // Step 1: Find which raw metric feeds into this classification
        String sourceMetricName = metricClassificationService.getSourceMetricName(classificationName);

        // Step 2: Calculate the source metric value
        ComputedMetric sourceMetric = getCachedMetric(sourceMetricName);
        Object sourceValue = calculateMetricValue(employee, sourceMetric);

        // Step 3: Classify the computed value
        Map<String, Object> result = metricClassificationService.classifyMetric(classificationName, sourceValue);

        if (result == null) {
            log.warn("No matching classification rule for employee: {}, classification: {}",
                    employee.getId(), classificationName);
            return null;
        }

        // Return the classification level (integer)
        return result.get("level");
    }

    // ── EXTENSION ───────────────────────────────────────────────────────

    /**
     * EXTENSION method: Placeholder for custom service calls (future feature).
     */
    private Object calculateFromExtension(Long entityId, String extensionDef) {
        log.warn("EXTENSION method not yet implemented for entityId: {}", entityId);
        return null;
    }
}
