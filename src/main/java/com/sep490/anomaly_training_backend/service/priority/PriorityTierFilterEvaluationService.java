package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PriorityTier;

import java.util.List;
import java.util.Map;

public interface PriorityTierFilterEvaluationService {
    boolean evaluateTierFilters(PriorityTier tier, Employee employee, Map<String, Object> employeeMetrics);

    Map<Long, Boolean> batchEvaluateTierFilters(PriorityTier tier, List<Employee> employees, Map<Long, Map<String, Object>> employeesMetrics);
}
