package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;

import java.util.List;

/**
 * Main service để tính toán Priority Scoring
 * <p>
 * Quy trình:
 * 1. Load Policy + Tiers + Filters
 * 2. Tính toán metrics cho tất cả employees
 * 3. Evaluate filters của từng tier
 * 4. Sắp xếp employees trong tier
 * 5. Lưu priority snapshots
 */
public interface PriorityScoringService {

    /**
     * Generate priority snapshot cho policy + team
     *
     * @param policyId  Priority policy ID
     * @param teamId    Team ID
     * @param employees List employees cần scoring
     * @return Priority snapshot
     */
    PrioritySnapshot generateSnapshot(Long policyId, Long teamId, List<Employee> employees);

    /**
     * Recalculate priorities cho team theo policy
     *
     * @param policyId Priority policy ID
     * @param teamId   Team ID
     * @return Updated snapshot
     */
    PrioritySnapshot recalculatePriorities(Long policyId, Long teamId);

    /**
     * Get latest snapshot cho policy + team
     *
     * @param policyId
     * @param teamId
     * @return Latest snapshot hoặc null
     */
    PrioritySnapshot getLatestSnapshot(Long policyId, Long teamId);

    /**
     * Delete snapshot
     */
    void deleteSnapshot(Long snapshotId);
}