package com.sep490.anomaly_training_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request để generate optimal training schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateTrainingScheduleRequest {

    /**
     * Training Plan ID
     */
    private Long trainingPlanId;

    /**
     * Priority Snapshot ID (để lấy danh sách employees + priority ranking)
     */
    private Long prioritySnapshotId;

    /**
     * Calendar year để lấy factory calendar
     */
    private Integer calendarYear;

    /**
     * Strategy (optional)
     * - GREEDY: Allocate from highest priority first (default)
     * - BALANCE: Balance load across days
     * - MINIMIZE_GAPS: Minimize gaps between training slots
     */
    @Builder.Default
    private String strategy = "GREEDY";

    /**
     * Override min/max per day (optional)
     */
    private Integer minPerDay;
    private Integer maxPerDay;
}