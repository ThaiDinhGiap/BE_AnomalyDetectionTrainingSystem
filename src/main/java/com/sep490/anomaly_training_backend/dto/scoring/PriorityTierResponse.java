package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sep490.anomaly_training_backend.enums.FilterLogic;
import com.sep490.anomaly_training_backend.enums.RankingDirection;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriorityTierResponse {

    Long id;
    Integer tierOrder;
    String tierName;
    FilterLogic filterLogic;
    String rankingMetric;
    RankingDirection rankingDirection;
    String secondaryMetric;
    RankingDirection secondaryDirection;
    Boolean isActive;
    List<TierFilterResponse> filters;
}
