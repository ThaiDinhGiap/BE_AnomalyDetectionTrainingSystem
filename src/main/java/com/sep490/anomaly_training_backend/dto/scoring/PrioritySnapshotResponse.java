package com.sep490.anomaly_training_backend.dto.scoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response của priority snapshot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrioritySnapshotResponse {

    /**
     * Snapshot ID
     */
    private Long id;

    /**
     * Team info
     */
    private Long teamId;
    private String teamName;

    /**
     * Policy info
     */
    private Long policyId;
    private String policyCode;
    private String policyName;

    /**
     * Total employees in snapshot
     */
    private Integer totalEmployees;

    /**
     * Details grouped by tier
     */
    private List<TierGroupDto> tierGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierGroupDto {
        private Integer tierOrder;
        private String tierName;
        private Integer employeeCount;
        private List<PrioritySnapshotDetailDto> details;
    }
}