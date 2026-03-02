package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sep490.anomaly_training_backend.enums.FilterOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TierFilterResponse {

    Long id;
    String metricName;
    String metricDisplayName;
    FilterOperator operator;
    String operatorSymbol;
    String filterValue;
    String filterUnit;
    Integer filterOrder;
}
