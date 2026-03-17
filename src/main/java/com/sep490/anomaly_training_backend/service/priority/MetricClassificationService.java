package com.sep490.anomaly_training_backend.service.priority;

import java.util.List;
import java.util.Map;

public interface MetricClassificationService {
    Map<String, Object> classifyMetric(String classificationName, Object metricValue);

    List<Map<String, Object>> classifyBatch(String classificationName, List<Object> metricValues);

}
