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

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriorityTierRequest {

    @NotNull(message = "Tier order is required")
    Integer tierOrder;

    @NotBlank(message = "Tier name is required")
    String tierName;

    @NotBlank(message = "Filter logic is required")
    String filterLogic;

    @NotBlank(message = "Ranking metric is required")
    String rankingMetric;

    @NotBlank(message = "Ranking direction is required")
    String rankingDirection;

    String secondaryMetric;

    String secondaryDirection;

    @NotEmpty(message = "At least one filter is required")
    @Valid
    List<TierFilterRequest> filters;
}
