package com.sep490.anomaly_training_backend.dto.scoring;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriorityPolicyRequest {

    @NotBlank(message = "Policy name is required")
    String policyName;

    @NotBlank(message = "Entity type is required")
    String entityType;

    @NotNull(message = "Effective date is required")
    LocalDate effectiveDate;

    LocalDate expirationDate;

    String description;

    @NotEmpty(message = "At least one tier is required")
    @Valid
    List<PriorityTierRequest> tiers;
}
