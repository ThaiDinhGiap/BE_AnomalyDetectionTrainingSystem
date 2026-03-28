package com.sep490.anomaly_training_backend.service.priority;

import java.util.List;
import java.util.Map;

public interface MetricClassificationService {
    Map<String, Object> classifyMetric(String classificationName, Object metricValue);

    List<Map<String, Object>> classifyBatch(String classificationName, List<Object> metricValues);

    /**
     * Get the source metric name used by a classification group.
     * This is the metric_source column from the first rule in the classification.
     */
    String getSourceMetricName(String classificationName);
}
