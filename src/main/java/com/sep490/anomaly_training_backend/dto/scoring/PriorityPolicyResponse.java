package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriorityPolicyResponse {

    Long id;
    String policyCode;
    String policyName;
    PolicyEntityType entityType;
    LocalDate effectiveDate;
    LocalDate expirationDate;
    PolicyStatus status;
    String description;
    List<PriorityTierResponse> tiers;
    LocalDateTime createdAt;
    String createdBy;
    LocalDateTime updatedAt;
    String updatedBy;
}
