package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response của priority snapshot detail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrioritySnapshotDetailDto {

    /**
     * Detail ID
     */
    private Long id;

    /**
     * Employee info
     */
    private Long employeeId;
    private String employeeCode;
    private String fullName;

    /**
     * Tier info
     */
    private Integer tierOrder;
    private String tierName;

    /**
     * Ranking info
     */
    private Integer sortRank;

    /**
     * Metrics values
     */
    private Map<String, Object> metricValues;

    /**
     * Priority tags (optional)
     */
    private Map<String, String> priorityTags;
}