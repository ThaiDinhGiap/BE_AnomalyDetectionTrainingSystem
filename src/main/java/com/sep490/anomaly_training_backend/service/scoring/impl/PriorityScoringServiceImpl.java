package com.sep490.anomaly_training_backend.service.scoring.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.PriorityPolicyRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotDetailRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.service.scoring.ComputedMetricService;
import com.sep490.anomaly_training_backend.service.scoring.PriorityScoringService;
import com.sep490.anomaly_training_backend.service.scoring.PriorityTierFilterEvaluationService;
import com.sep490.anomaly_training_backend.service.scoring.PriorityTierRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation của PriorityScoringService
 * <p>
 * Luồng xử lý:
 * 1. Load Policy + Tiers + Filters từ DB
 * 2. Tính metrics cho tất cả employees (batch)
 * 3. Duyệt từng tier:
 * a. Evaluate filters → filter employees
 * b. Rank employees trong tier
 * 4. Lưu results vào priority_snapshots + priority_snapshot_details
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PriorityScoringServiceImpl implements PriorityScoringService {

    // Repositories
    private final PriorityPolicyRepository policyRepository;
    private final PrioritySnapshotRepository snapshotRepository;
    private final PrioritySnapshotDetailRepository snapshotDetailRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    // Services
    private final ComputedMetricService metricCalculationService;
    private final PriorityTierFilterEvaluationService filterEvaluationService;
    private final PriorityTierRankingService rankingService;

    private final ObjectMapper objectMapper;

    /**
     * Generate priority snapshot
     */
    @Override
    public PrioritySnapshot generateSnapshot(Long policyId, Long teamId, List<Employee> employees) {
        log.info("Generating priority snapshot for policy: {}, team: {}, employees count: {}",
                policyId, teamId, employees.size());

        // 1. Load policy
        PriorityPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new AppException(ErrorCode.POLICY_NOT_FOUND,
                        "Policy not found: " + policyId));

        // 2. Load team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND,
                        "Team not found: " + teamId));

        // 3. Collect all metric names từ tất cả tiers
        Set<String> metricsNeeded = collectMetricsFromPolicy(policy);

        // 4. Tính metrics cho tất cả employees (batch)
        log.info("Calculating metrics for {} employees", employees.size());
        Map<Long, Map<String, Object>> employeesMetrics =
                metricCalculationService.batchCalculateMetrics(employees, metricsNeeded);

        // 5. Tạo snapshot
        PrioritySnapshot snapshot = PrioritySnapshot.builder()
                .team(team)
                .policy(policy)
                .policySnapshot(serializePolicy(policy))
                .build();

        PrioritySnapshot savedSnapshot = snapshotRepository.save(snapshot);

        // 6. Duyệt từng tier và generate snapshot details
        List<PriorityTier> tiers = policy.getTiers().stream()
                .filter(tier -> tier.getIsActive() && !tier.isDeleteFlag())
                .sorted((t1, t2) -> Integer.compare(t1.getTierOrder(), t2.getTierOrder()))
                .toList();

        int globalRank = 1;
        for (PriorityTier tier : tiers) {
            log.info("Processing tier: {} (order: {})", tier.getTierName(), tier.getTierOrder());

            // Evaluate filters → lấy employees fit tier này
            List<Employee> tierEmployees = employees.stream()
                    .filter(emp -> filterEvaluationService.evaluateTierFilters(tier, emp, employeesMetrics.get(emp.getId())))
                    .toList();

            // Rank employees trong tier
            List<PriorityTierRankingService.EmployeeRankingResult> rankedEmployees =
                    rankingService.rankEmployees(tier, tierEmployees, employeesMetrics);

            // Save snapshot details
            for (PriorityTierRankingService.EmployeeRankingResult result : rankedEmployees) {
                PrioritySnapshotDetail detail = PrioritySnapshotDetail.builder()
                        .snapshot(savedSnapshot)
                        .employee(result.employee)
                        .employeeCode(result.getEmployeeCode())
                        .fullName(result.getFullName())
                        .tierOrder(tier.getTierOrder())
                        .tierName(tier.getTierName())
                        .sortRank(result.sortRank)
                        .metricValues(serializeMetrics(result.metricValues))
                        .build();

                snapshotDetailRepository.save(detail);
            }

            globalRank += rankedEmployees.size();
        }

        log.info("Priority snapshot generated successfully. Snapshot ID: {}", savedSnapshot.getId());
        return savedSnapshot;
    }

    /**
     * Recalculate priorities cho team
     */
    @Override
    public PrioritySnapshot recalculatePriorities(Long policyId, Long teamId) {
        log.info("Recalculating priorities for policy: {}, team: {}", policyId, teamId);

        // Get employees của team
        List<Employee> employees = employeeRepository.findByTeamIdAndDeleteFlagFalse(teamId);

        if (employees.isEmpty()) {
            log.warn("No employees found for team: {}", teamId);
        }

        // Delete old snapshots
        List<PrioritySnapshot> oldSnapshots = snapshotRepository.findByPolicyIdAndTeamId(policyId, teamId);
        for (PrioritySnapshot snapshot : oldSnapshots) {
            snapshotDetailRepository.deleteBySnapshotId(snapshot.getId());
            snapshotRepository.delete(snapshot);
        }

        // Generate new snapshot
        return generateSnapshot(policyId, teamId, employees);
    }

    /**
     * Get latest snapshot
     */
    @Override
    @Transactional(readOnly = true)
    public PrioritySnapshot getLatestSnapshot(Long policyId, Long teamId) {
        return snapshotRepository.findLatestByPolicyIdAndTeamId(policyId, teamId);
    }

    /**
     * Delete snapshot
     */
    @Override
    public void deleteSnapshot(Long snapshotId) {
        PrioritySnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new AppException(ErrorCode.SNAPSHOT_NOT_FOUND,
                        "Snapshot not found: " + snapshotId));

        // Delete details first
        snapshotDetailRepository.deleteBySnapshotId(snapshotId);

        // Delete snapshot
        snapshotRepository.delete(snapshot);

        log.info("Snapshot deleted: {}", snapshotId);
    }

    /**
     * Helper: Collect all metric names từ policy
     */
    private Set<String> collectMetricsFromPolicy(PriorityPolicy policy) {
        Set<String> metrics = new java.util.HashSet<>();

        for (PriorityTier tier : policy.getTiers()) {
            // Ranking metric
            metrics.add(tier.getRankingMetric());

            // Secondary metric
            if (tier.getSecondaryMetric() != null && !tier.getSecondaryMetric().isBlank()) {
                metrics.add(tier.getSecondaryMetric());
            }

            // Filter metrics
            for (var filter : tier.getFilters()) {
                metrics.add(filter.getMetricName());
            }
        }

        return metrics;
    }

    /**
     * Helper: Serialize policy to JSON
     */
    private String serializePolicy(PriorityPolicy policy) {
        try {
            return objectMapper.writeValueAsString(policy);
        } catch (Exception e) {
            log.error("Error serializing policy", e);
            return "{}";
        }
    }

    /**
     * Helper: Serialize metrics to JSON
     */
    private String serializeMetrics(Map<String, Object> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            log.error("Error serializing metrics", e);
            return "{}";
        }
    }

    /**
     * Helper: Get current username
     */
    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}