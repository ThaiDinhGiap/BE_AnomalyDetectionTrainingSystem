package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.repository.ComputedMetricRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.service.priority.MetricResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MetricResolverServiceImpl implements MetricResolverService {

    private final ComputedMetricRepository metricRepo;
    private final JdbcTemplate jdbc;
    private final EmployeeRepository empRepo;

    @Override
    public Map<Long, Map<String, Object>> batchCompute(List<Employee> employees, Set<String> neededMetrics) {
        return Map.of();
    }
}
