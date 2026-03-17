package com.sep490.anomaly_training_backend.dto.request;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TrainingSampleReviewPolicyRequest {
    String description;
    Long productLineId;
    String policyName;
    List<TrainingSampleReviewConfigRequest> reviewConfigs;
}
