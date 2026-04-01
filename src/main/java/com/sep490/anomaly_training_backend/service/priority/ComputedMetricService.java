package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.model.Employee;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ComputedMetricService {
    Map<Long, Map<String, Object>> batchCalculateMetrics(List<Employee> employees, Set<String> metricNames);
}
