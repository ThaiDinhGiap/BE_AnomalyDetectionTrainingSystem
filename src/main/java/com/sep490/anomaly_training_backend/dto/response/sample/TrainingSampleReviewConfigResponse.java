package com.sep490.anomaly_training_backend.dto.response.sample;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleReviewConfigResponse {
    Long id;
    Integer triggerMonth;
    Integer triggerDay;
    Integer dueDays;
}
