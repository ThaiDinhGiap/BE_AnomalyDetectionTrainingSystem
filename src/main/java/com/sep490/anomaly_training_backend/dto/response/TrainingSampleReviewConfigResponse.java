package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleReviewConfigResponse {
    Long id;
    String productLine;
    Integer triggerMonth = 3;
    Integer triggerDay = 1;
    Integer dueDays = 30;
    String assignee;
    Boolean isActive = true;

}
