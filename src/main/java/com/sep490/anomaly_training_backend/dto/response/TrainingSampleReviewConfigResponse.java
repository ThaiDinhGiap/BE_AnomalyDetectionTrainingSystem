package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleReviewConfigResponse {
    Long id;
    TrainingSampleReviewPolicyResponse reviewPolicy;
    ProductLineResponse productLine;
    Integer triggerMonth;
    Integer triggerDay;
    Integer dueDays;
}
