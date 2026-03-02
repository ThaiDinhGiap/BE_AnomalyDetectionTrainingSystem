package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sep490.anomaly_training_backend.enums.ComputeMethod;
import com.sep490.anomaly_training_backend.enums.MetricReturnType;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputedMetricResponse {

    Long id;
    String metricName;
    String displayName;
    PolicyEntityType entityType;
    ComputeMethod computeMethod;
    MetricReturnType returnType;
    String unit;
    String description;
    Boolean isActive;
}
