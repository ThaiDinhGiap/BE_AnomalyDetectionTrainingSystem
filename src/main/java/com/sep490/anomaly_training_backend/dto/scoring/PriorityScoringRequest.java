package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request để generate priority snapshot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriorityScoringRequest {

    /**
     * Priority Policy ID
     */
    private Long policyId;

    /**
     * Team ID
     */
    private Long teamId;

    /**
     * Filter employees (optional)
     * Nếu null, lấy tất cả employees của team
     */
    private List<Long> employeeIds;

    /**
     * From date (optional)
     */
    private LocalDate fromDate;

    /**
     * To date (optional)
     */
    private LocalDate toDate;

    /**
     * Include metrics detail (default: true)
     */
    @Builder.Default
    private Boolean includeMetrics = true;
}