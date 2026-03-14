package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.model.Employee;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetricResolverService {
    Map<Long, Map<String, Object>> batchCompute(List<Employee> employees, Set<String> neededMetrics);
    
}
