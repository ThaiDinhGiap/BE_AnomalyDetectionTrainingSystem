package com.sep490.anomaly_training_backend.dto;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Summary statistics for training plan schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSummary {
    /**
     * Total training sessions allocated
     */
    private int totalSlots;

    /**
     * Total number of days used
     */
    private int totalDays;

    /**
     * Average slots per day
     */
    private double avgSlotsPerDay;

    /**
     * Count of slots per date (for chart/visualization)
     */
    private Map<LocalDate, Long> countByDate;

    /**
     * Count of slots per status (PENDING, DONE, etc.)
     */
    private Map<ReportStatus, Long> countByStatus;

    /**
     * Number of unique employees trained (at least once)
     */
    private int uniqueEmployeesTrained;

    /**
     * Total number of employee_skills allocated across all passes
     */
    private int totalSkillsAllocated;

    /**
     * Average skills trained per employee
     */
    private double avgSkillsPerEmployee;

    /**
     * Calculate utilization percentage
     */
    public double getUtilizationPercent() {
        if (totalDays == 0) return 0;
        // Assuming 5 is max slots per day for calculation
        return (totalSlots * 100.0) / (totalDays * 5.0);
    }

    /**
     * Calculate average skills per employee
     */
    public void calculateAvgSkillsPerEmployee() {
        if (uniqueEmployeesTrained == 0) {
            this.avgSkillsPerEmployee = 0;
        } else {
            this.avgSkillsPerEmployee = (double) totalSkillsAllocated / uniqueEmployeesTrained;
        }
    }
}
