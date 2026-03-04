package com.sep490.anomaly_training_backend.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleReviewRequest {
    Long id;
    String sampleSnapshot;
}
