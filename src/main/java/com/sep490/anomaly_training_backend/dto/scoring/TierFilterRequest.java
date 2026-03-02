package com.sep490.anomaly_training_backend.dto.scoring;

import jakarta.validation.constraints.NotBlank;
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
public class TierFilterRequest {

    @NotBlank(message = "Metric name is required")
    String metricName;

    @NotBlank(message = "Operator is required")
    String operator;

    @NotBlank(message = "Filter value is required")
    String value;

    Integer filterOrder;
}
