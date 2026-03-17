package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.service.priority.ComputedMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ComputedMetricServiceImpl implements ComputedMetricService {

    private final ComputedMetricRepository computedMetricRepository;
    private final PriorityPolicyMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final TrainingResultDetailRepository trainingResultDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ComputedMetricResponse> getMetricsByEntityType(PolicyEntityType entityType) {
        return mapper.toMetricResponseList(
                computedMetricRepository.findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(entityType)
        );
    }

    @Override
    public Object calculateMetric(Employee employee, String metricName) {
        ComputedMetric metric = computedMetricRepository.findByMetricNameAndDeleteFlagFalse(metricName)
                .orElseThrow(() -> new AppException(ErrorCode.METRIC_NOT_FOUND,
                        "Metric not found: " + metricName));

        return calculateMetricValue(employee.getId(), metric);
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

    @Override
    public Map<Long, Map<String, Object>> batchCalculateMetrics(List<Employee> employees, Set<String> metricNames) {
        return employees.stream()
                .collect(Collectors.toMap(
                        Employee::getId,
                        emp -> calculateAllMetrics(emp, metricNames)
                ));
    }

    // PRIVATE METHODS

    /**
     * Tính giá trị của metric dựa trên compute method
     */
    private Object calculateMetricValue(Long entityId, ComputedMetric metric) {
        try {
            return switch (metric.getComputeMethod()) {
                case PROPERTY -> calculateFromProperty(entityId, metric.getComputeDefinition());
                case SQL -> calculateFromSQL(entityId, metric.getComputeDefinition(), metric.getReturnType());
                case EXTENSION -> calculateFromExtension(entityId, metric.getComputeDefinition());
                case CLASSIFICATION -> calculateFromClassification(entityId, metric.getComputeDefinition());
                default -> throw new AppException(ErrorCode.INVALID_METRIC_METHOD,
                        "Unknown compute method: " + metric.getComputeMethod());
            };
        } catch (Exception e) {
            log.error("Error calculating metric: {}", metric.getMetricName(), e);
            throw new AppException(ErrorCode.METRIC_CALCULATION_ERROR,
                    "Failed to calculate metric: " + metric.getMetricName());
        }
    }

    /**
     * PROPERTY method: Lấy giá trị từ property của entity
     * VD: "employee.yearsOfService" → employee.getYearsOfService()
     * <p>
     * Hiện tại chỉ support Employee entity, có thể extend sau
     */
    private Object calculateFromProperty(Long entityId, String propertyPath) {
        // Parse property path: "employee.fieldName" hoặc "process.fieldName"
        String[] parts = propertyPath.split("\\.");
        if (parts.length != 2) {
            throw new AppException(ErrorCode.INVALID_METRIC_DEFINITION,
                    "Invalid property path: " + propertyPath);
        }

        String entityType = parts[0];
        String fieldName = parts[1];

        if (!"employee".equals(entityType)) {
            throw new AppException(ErrorCode.INVALID_METRIC_DEFINITION,
                    "PROPERTY method currently only supports 'employee' entity type");
        }

        // Lấy Employee entity từ DB (có thể cache nếu cần)
        // Note: Thực tế cần implement Employee Repository query
        // Tạm thời return null - cần bổ sung

        log.warn("PROPERTY method not fully implemented for: {}", propertyPath);
        return null;
    }

    /**
     * SQL method: Thực thi SQL query với parameter
     * VD: "SELECT DATEDIFF(CURDATE(), MAX(trd.actual_date)) FROM training_result_details trd WHERE trd.employee_id = :entityId AND trd.is_pass = TRUE"
     *
     * @param entityId   Employee/Process ID
     * @param sqlQuery   SQL query với :entityId parameter
     * @param returnType Kiểu dữ liệu trả về
     */
    private Object calculateFromSQL(Long entityId, String sqlQuery, MetricReturnType returnType) {
        // Replace :entityId parameter
        String finalQuery = sqlQuery.replace(":entityId", entityId.toString());

        try {
            return switch (returnType) {
                case INT -> jdbcTemplate.queryForObject(finalQuery, Integer.class);
                case DECIMAL -> jdbcTemplate.queryForObject(finalQuery, BigDecimal.class);
                case BOOLEAN -> jdbcTemplate.queryForObject(finalQuery, Boolean.class);
                case STRING -> jdbcTemplate.queryForObject(finalQuery, String.class);
                default -> null;
            };
        } catch (Exception e) {
            log.error("SQL metric calculation failed. Query: {}", finalQuery, e);
            return null;
        }
    }

    /**
     * EXTENSION method: Gọi extension service (Future feature)
     * <p>
     * Có thể dùng để gọi custom services, APIs, v.v.
     */
    private Object calculateFromExtension(Long entityId, String extensionDef) {
        // TODO: Implement extension service calling
        // Có thể parse extensionDef để xác định service nào cần gọi
        log.warn("EXTENSION method not yet implemented for entityId: {}", entityId);
        return null;
    }

    /**
     * CLASSIFICATION method: Áp dụng metric classification rules
     * <p>
     * Sử dụng MetricClassificationService để classify metric value
     * VD: days_since_last_training = 90 → classification_level = "HIGH_PRIORITY"
     */
    private Object calculateFromClassification(Long entityId, String classificationDef) {
        // TODO: Implement classification logic
        // classificationDef có format: "classification_type" (VD: "training_priority")
        // Cần gọi MetricClassificationService để lấy rules
        log.warn("CLASSIFICATION method not yet implemented for entityId: {}", entityId);
        return null;
    }
}
