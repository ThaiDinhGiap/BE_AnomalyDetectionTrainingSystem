package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.enums.ComputeMethod;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.PriorityPolicyMapper;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.MetricClassification;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.repository.MetricClassificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputedMetricServiceImplTest {

    @Mock
    private ComputedMetricRepository computedMetricRepository;

    @Mock
    private MetricClassificationRepository classificationRepository;

    @Mock
    private PriorityPolicyMapper mapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ComputedMetricServiceImpl computedMetricService;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        employee1 = new Employee();
        employee1.setId(1L);
        employee1.setEmployeeCode("E001");

        employee2 = new Employee();
        employee2.setId(2L);
        employee2.setEmployeeCode("E002");
    }

    @Test
    void batchCalculateMetrics_withPropertyMethod_shouldReadFromEmployeeObject() {
        // Arrange
        String metricName = "employeeCodeMetric";
        ComputedMetric metric = new ComputedMetric();
        metric.setMetricName(metricName);
        metric.setComputeMethod(ComputeMethod.PROPERTY);
        metric.setComputeDefinition("employee.employeeCode");

        when(computedMetricRepository.findByMetricNameAndDeleteFlagFalse(metricName))
                .thenReturn(Optional.of(metric));

        // Act
        Map<Long, Map<String, Object>> results = computedMetricService.batchCalculateMetrics(
                List.of(employee1, employee2), Set.of(metricName));

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(1L)).containsEntry(metricName, "E001");
        assertThat(results.get(2L)).containsEntry(metricName, "E002");
    }

    @Test
    void batchCalculateMetrics_withPropertyMethod_invalidProperty_shouldBeNull() {
        // Arrange
        String metricName = "invalidMetric";
        ComputedMetric metric = new ComputedMetric();
        metric.setMetricName(metricName);
        metric.setComputeMethod(ComputeMethod.PROPERTY);
        metric.setComputeDefinition("employee.doesNotExist");

        when(computedMetricRepository.findByMetricNameAndDeleteFlagFalse(metricName))
                .thenReturn(Optional.of(metric));

        // Act
        Map<Long, Map<String, Object>> results = computedMetricService.batchCalculateMetrics(
                List.of(employee1), Set.of(metricName));

        // Assert
        assertThat(results.get(1L)).containsEntry(metricName, null);
    }

    @Test
    void batchCalculateMetrics_withSqlMethod_shouldUseBatchQueryIfPossible() {
        // Arrange
        String metricName = "sqlMetric";
        ComputedMetric metric = new ComputedMetric();
        metric.setMetricName(metricName);
        metric.setComputeMethod(ComputeMethod.SQL);
        // This is a simple query format that the batch parser supports
        metric.setComputeDefinition("SELECT COUNT(*) FROM defect_proposals WHERE employee_id = :entityId");
        metric.setReturnType(MetricReturnType.INT);

        when(computedMetricRepository.findByMetricNameAndDeleteFlagFalse(metricName))
                .thenReturn(Optional.of(metric));

        // Mock batch query
        List<Map<String, Object>> mockRows = List.of(
                Map.of("employee_id", 1L, "metric_value", 5),
                Map.of("employee_id", 2L, "metric_value", 10)
        );
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(mockRows);

        // Act
        Map<Long, Map<String, Object>> results = computedMetricService.batchCalculateMetrics(
                List.of(employee1, employee2), Set.of(metricName));

        // Assert
        assertThat(results.get(1L)).containsEntry(metricName, 5);
        assertThat(results.get(2L)).containsEntry(metricName, 10);
    }

    @Test
    void batchCalculateMetrics_withClassificationMethod_shouldApplyRulesCorrectly() {
        // Arrange
        String sourceMetricName = "rawScore";
        String classificationName = "score_level";

        // 1. Setup Source Metric (PROPERTY)
        ComputedMetric sourceMetric = new ComputedMetric();
        sourceMetric.setMetricName(sourceMetricName);
        sourceMetric.setComputeMethod(ComputeMethod.PROPERTY);
        // Assume employee has an ID which we treat as a score just for mocking
        sourceMetric.setComputeDefinition("employee.id");

        // 2. Setup Classification Metric
        ComputedMetric classMetric = new ComputedMetric();
        classMetric.setMetricName(classificationName);
        classMetric.setComputeMethod(ComputeMethod.CLASSIFICATION);
        classMetric.setComputeDefinition(classificationName); // compute_def for classification is the classification group name

        // Repositories
        when(computedMetricRepository.findByMetricNameAndDeleteFlagFalse(sourceMetricName))
                .thenReturn(Optional.of(sourceMetric));
        when(computedMetricRepository.findByMetricNameAndDeleteFlagFalse(classificationName))
                .thenReturn(Optional.of(classMetric));

        // 3. Setup Classification Rules
        MetricClassification rule1 = new MetricClassification();
        rule1.setMetricSource(sourceMetricName);
        rule1.setConditionExpression("value >= 2");
        rule1.setOutputLevel(1); // high
        rule1.setOutputLabel("High");
        rule1.setPriority(1);
        
        MetricClassification rule2 = new MetricClassification();
        rule2.setMetricSource(sourceMetricName);
        rule2.setConditionExpression("value < 2");
        rule2.setOutputLevel(2); // low
        rule2.setOutputLabel("Low");
        rule2.setPriority(2);

        when(classificationRepository.findByClassificationNameAndIsActiveTrueOrderByPriority(classificationName))
                .thenReturn(List.of(rule1, rule2));

        // Act
        // employee1.id = 1 -> should match rule2 (outputLevel = 2)
        // employee2.id = 2 -> should match rule1 (outputLevel = 1)
        Map<Long, Map<String, Object>> results = computedMetricService.batchCalculateMetrics(
                List.of(employee1, employee2), Set.of(classificationName));

        // Assert
        assertThat(results.get(1L)).containsEntry(classificationName, 2);
        assertThat(results.get(2L)).containsEntry(classificationName, 1);
    }

    @Test
    void batchCalculateMetrics_metricNotFound_shouldThrowAppException() {
        // Arrange
        String metricName = "unknownMetric";
        when(computedMetricRepository.findByMetricNameAndDeleteFlagFalse(metricName))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> computedMetricService.batchCalculateMetrics(List.of(employee1), Set.of(metricName)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Metric not found: " + metricName);
    }
}
