package com.sep490.anomaly_training_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Response sau khi generate schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingScheduleGenerationResponse {

    /**
     * Training Plan ID
     */
    private Long trainingPlanId;

    /**
     * Total slots allocated
     */
    private Integer totalSlots;

    /**
     * Total days used
     */
    private Integer totalDays;

    /**
     * Average slots per day
     */
    private Double avgSlotsPerDay;

    /**
     * Employees not scheduled (nếu không đủ slot)
     */
    private Integer notScheduledCount;

    /**
     * Count by date (để chart/visualization)
     */
    private Map<LocalDate, Long> countByDate;

    /**
     * Count by status
     */
    private Map<ReportStatus, Long> countByStatus;

    /**
     * Generation timestamp
     */
    private String generatedAt;

    /**
     * Status message
     */
    private String message;
}