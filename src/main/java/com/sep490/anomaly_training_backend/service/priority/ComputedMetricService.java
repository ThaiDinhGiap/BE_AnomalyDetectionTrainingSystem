package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.dto.scoring.ComputedMetricResponse;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.model.Employee;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ComputedMetricService {
    List<ComputedMetricResponse> getMetricsByEntityType(PolicyEntityType entityType);

    Object calculateMetric(Employee employee, String metricName);

    Map<String, Object> calculateAllMetrics(Employee employee, Set<String> metricNames);

    Map<Long, Map<String, Object>> batchCalculateMetrics(List<Employee> employees, Set<String> metricNames);


}
