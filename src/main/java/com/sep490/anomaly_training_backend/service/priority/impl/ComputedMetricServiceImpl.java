package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.enums.ComputeMethod;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.MetricClassification;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.repository.MetricClassificationRepository;
import com.sep490.anomaly_training_backend.service.priority.ComputedMetricService;
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
    private final MetricClassificationRepository classificationRepository;
    private final PriorityPolicyMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * In-memory cache for metric definitions (they rarely change).
     * Key: metricName, Value: ComputedMetric entity
     */
    private final ConcurrentHashMap<String, ComputedMetric> metricCache = new ConcurrentHashMap<>();


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
     */
    private Object calculateFromSQL(Long entityId, String sqlTemplate, MetricReturnType returnType) {
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
     */
    private Map<Long, Object> batchCalculateSQL(List<Long> employeeIds, ComputedMetric metric) {
        Map<Long, Object> results = new HashMap<>();

        if (employeeIds.isEmpty()) {
            return results;
        }

        String sqlTemplate = metric.getComputeDefinition();

        try {
            String inPlaceholders = employeeIds.stream().map(id -> "?").collect(Collectors.joining(","));

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

    // ── CLASSIFICATION (inlined from MetricClassificationService) ────────

    /**
     * CLASSIFICATION method: Apply metric classification rules.
     */
    private Object calculateFromClassification(Employee employee, String classificationName) {
        // Step 1: Find which raw metric feeds into this classification
        String sourceMetricName = getSourceMetricName(classificationName);

        // Step 2: Calculate the source metric value
        ComputedMetric sourceMetric = getCachedMetric(sourceMetricName);
        Object sourceValue = calculateMetricValue(employee, sourceMetric);

        // Step 3: Classify the computed value
        Map<String, Object> result = classifyMetric(classificationName, sourceValue);

        if (result == null) {
            log.warn("No matching classification rule for employee: {}, classification: {}",
                    employee.getId(), classificationName);
            return null;
        }

        return result.get("level");
    }

    /**
     * Classify 1 metric value theo classification rules.
     *
     * @param classificationName Tên classification (VD: "training_priority")
     * @param metricValue        Giá trị metric cần classify
     * @return Map {level, label} hoặc null nếu không match
     */
    private Map<String, Object> classifyMetric(String classificationName, Object metricValue) {
        List<MetricClassification> rules = classificationRepository
                .findByClassificationNameAndIsActiveTrueOrderByPriority(classificationName);

        if (rules.isEmpty()) {
            throw new AppException(ErrorCode.CLASSIFICATION_NOT_FOUND,
                    "Classification rules not found: " + classificationName);
        }

        for (MetricClassification rule : rules) {
            if (evaluateClassificationCondition(rule.getConditionExpression(), metricValue)) {
                return Map.of(
                        "level", rule.getOutputLevel(),
                        "label", rule.getOutputLabel() != null ? rule.getOutputLabel() : "",
                        "priority", rule.getPriority()
                );
            }
        }

        log.warn("No matching classification rule for: {} with value: {}", classificationName, metricValue);
        return null;
    }

    /**
     * Get the source metric name used by a classification group.
     */
    private String getSourceMetricName(String classificationName) {
        List<MetricClassification> rules = classificationRepository
                .findByClassificationNameAndIsActiveTrueOrderByPriority(classificationName);

        if (rules.isEmpty()) {
            throw new AppException(ErrorCode.CLASSIFICATION_NOT_FOUND,
                    "Classification rules not found: " + classificationName);
        }

        return rules.get(0).getMetricSource();
    }

    /**
     * Đánh giá condition expression.
     * Support format: "value > 60", "value <= 30", "value == TRUE", etc.
     */
    private boolean evaluateClassificationCondition(String condition, Object actualValue) {
        try {
            condition = condition.trim();
            String[] parts = condition.split("\\s+");

            if (parts.length < 3 || !"value".equals(parts[0])) {
                throw new AppException(ErrorCode.INVALID_CLASSIFICATION_RULE,
                        "Invalid condition format: " + condition);
            }

            String operator = parts[1];
            String compareValueStr = parts[2];

            return switch (operator) {
                case ">" -> compareAsNumbers(actualValue, compareValueStr) > 0;
                case ">=" -> compareAsNumbers(actualValue, compareValueStr) >= 0;
                case "<" -> compareAsNumbers(actualValue, compareValueStr) < 0;
                case "<=" -> compareAsNumbers(actualValue, compareValueStr) <= 0;
                case "==" -> actualValue.toString().equals(compareValueStr);
                case "!=" -> !actualValue.toString().equals(compareValueStr);
                default -> throw new AppException(ErrorCode.INVALID_CLASSIFICATION_RULE,
                        "Unknown operator: " + operator);
            };
        } catch (Exception e) {
            log.error("Error evaluating condition: {} for value: {}", condition, actualValue, e);
            return false;
        }
    }

    /**
     * So sánh 2 giá trị dưới dạng số.
     */
    private int compareAsNumbers(Object val1, String val2Str) {
        try {
            double num1 = Double.parseDouble(val1.toString());
            double num2 = Double.parseDouble(val2Str);
            return Double.compare(num1, num2);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_CLASSIFICATION_VALUE,
                    "Cannot compare non-numeric values");
        }
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
