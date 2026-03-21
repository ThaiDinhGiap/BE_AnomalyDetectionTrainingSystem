package com.sep490.anomaly_training_backend.dto.response.sample;

import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TrainingSampleReviewPolicyResponse {
    Long id;
    String policyCode;
    String policyName;
    LocalDate effectiveDate;
    LocalDate expirationDate;
    PolicyStatus status;
    String description;
    ProductLineResponse productLine;
    List<TrainingSampleReviewConfigResponse> reviewConfigs;
}
