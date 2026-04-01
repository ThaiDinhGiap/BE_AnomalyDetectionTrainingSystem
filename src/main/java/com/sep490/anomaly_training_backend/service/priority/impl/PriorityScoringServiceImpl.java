package com.sep490.anomaly_training_backend.service.priority.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.enums.FilterLogic;
import com.sep490.anomaly_training_backend.enums.RankingDirection;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import com.sep490.anomaly_training_backend.model.PriorityTier;
import com.sep490.anomaly_training_backend.model.PriorityTierFilter;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.PriorityPolicyRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotDetailRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.service.priority.ComputedMetricService;
import com.sep490.anomaly_training_backend.service.priority.PriorityScoringService;
import com.sep490.anomaly_training_backend.util.PriorityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PriorityScoringServiceImpl implements PriorityScoringService {

    private final PriorityPolicyRepository policyRepository;
    private final PrioritySnapshotRepository snapshotRepository;
    private final PrioritySnapshotDetailRepository snapshotDetailRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    private final ComputedMetricService metricCalculationService;

    private final ObjectMapper objectMapper;

    @Override
    public PrioritySnapshot generateSnapshot(Long policyId, Long teamId, List<Employee> employees) {
        log.info("Generating priority snapshot for policy: {}, team: {}, employees count: {}",
                policyId, teamId, employees.size());

        PriorityPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new AppException(ErrorCode.POLICY_NOT_FOUND,
                        "Policy not found: " + policyId));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND,
                        "Team not found: " + teamId));

        Set<String> metricsNeeded = collectMetricsFromPolicy(policy);

        Map<Long, Map<String, Object>> employeesMetrics =
                metricCalculationService.batchCalculateMetrics(employees, metricsNeeded);

        PrioritySnapshot snapshot = PrioritySnapshot.builder()
                .team(team)
                .policy(policy)
                .policySnapshot(serializePolicy(policy))
                .build();
        PrioritySnapshot savedSnapshot = snapshotRepository.save(snapshot);

        List<PriorityTier> tiers = policy.getTiers().stream()
                .filter(tier -> tier.getIsActive() && !tier.isDeleteFlag())
                .sorted((t1, t2) -> Integer.compare(t1.getTierOrder(), t2.getTierOrder()))
                .toList();

        // Track employees đã được assign vào tier nào rồi
        // Mỗi employee chỉ thuộc 1 tier — tier đầu tiên mà họ khớp filter (tier order thấp = ưu tiên cao)
        Set<Long> assignedEmployeeIds = new HashSet<>();
        List<PrioritySnapshotDetail> detailsToSave = new ArrayList<>();

        for (PriorityTier tier : tiers) {
            log.info("Processing tier: {} (order: {})", tier.getTierName(), tier.getTierOrder());

            // Chỉ xét employees chưa được assign tier nào
            List<Employee> unassignedEmployees = employees.stream()
                    .filter(emp -> !assignedEmployeeIds.contains(emp.getId()))
                    .toList();

            List<Employee> tierEmployees = unassignedEmployees.stream()
                    .filter(emp -> evaluateTierFilters(
                            tier, emp, employeesMetrics.get(emp.getId())))
                    .toList();

            List<EmployeeRankingResult> rankedEmployees =
                    rankEmployees(tier, tierEmployees, employeesMetrics);

            for (EmployeeRankingResult result : rankedEmployees) {
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

                detailsToSave.add(detail);
                assignedEmployeeIds.add(result.employee.getId());
            }

        }

        // Employees không khớp tier nào vẫn phải được đưa vào snapshot
        // với tier đặc biệt "UNTIERED" — ưu tiên thấp nhất, xếp sau tất cả
        List<Employee> untieredEmployees = employees.stream()
                .filter(emp -> !assignedEmployeeIds.contains(emp.getId()))
                .toList();

        if (!untieredEmployees.isEmpty()) {
            log.info("{} employees không khớp tier nào → xếp vào UNTIERED (ưu tiên thấp nhất)",
                    untieredEmployees.size());

            List<Employee> sortedUntiered = untieredEmployees.stream()
                    .sorted((e1, e2) -> e1.getEmployeeCode().compareTo(e2.getEmployeeCode()))
                    .toList();

            int untieredRank = 1;
            int untieredTierOrder = tiers.isEmpty() ? 1 : tiers.get(tiers.size() - 1).getTierOrder() + 1;

            for (Employee emp : sortedUntiered) {
                PrioritySnapshotDetail detail = PrioritySnapshotDetail.builder()
                        .snapshot(savedSnapshot)
                        .employee(emp)
                        .employeeCode(emp.getEmployeeCode())
                        .fullName(emp.getFullName())
                        .tierOrder(untieredTierOrder)
                        .tierName("UNTIERED")
                        .sortRank(untieredRank)
                        .metricValues("{}")
                        .build();

                detailsToSave.add(detail);
                untieredRank++;
            }
        }

        snapshotDetailRepository.saveAll(detailsToSave);

        log.info("Snapshot generated: {} employees ({} tiered, {} untiered)",
                detailsToSave.size(), assignedEmployeeIds.size(), untieredEmployees.size());
        return savedSnapshot;
    }

    @Override
    public PrioritySnapshot recalculatePriorities(Long policyId, Long teamId) {
        log.info("Recalculating priorities for policy: {}, team: {}", policyId, teamId);

        List<Employee> employees = employeeRepository.findByTeamsIdAndDeleteFlagFalse(teamId);
        if (employees.isEmpty()) {
            log.warn("No employees found for team: {}", teamId);
        }

        List<PrioritySnapshot> oldSnapshots = snapshotRepository.findByPolicyIdAndTeamId(policyId, teamId);
        for (PrioritySnapshot snapshot : oldSnapshots) {
            snapshotDetailRepository.deleteBySnapshotId(snapshot.getId());
            snapshotRepository.delete(snapshot);
        }

        return generateSnapshot(policyId, teamId, employees);
    }

    @Override
    @Transactional(readOnly = true)
    public PrioritySnapshot getLatestSnapshot(Long policyId, Long teamId) {
        return snapshotRepository.findLatestByPolicyIdAndTeamId(policyId, teamId);
    }

    @Override
    public void deleteSnapshot(Long snapshotId) {
        PrioritySnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new AppException(ErrorCode.SNAPSHOT_NOT_FOUND,
                        "Snapshot not found: " + snapshotId));
        snapshotDetailRepository.deleteBySnapshotId(snapshotId);
        snapshotRepository.delete(snapshot);
        log.info("Snapshot deleted: {}", snapshotId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrioritySnapshot> listSnapshotsByPolicy(Long policyId) {
        return snapshotRepository.findByPolicyIdOrderByCreatedAtDesc(policyId);
    }

    @Override
    @Transactional(readOnly = true)
    public PrioritySnapshot getSnapshotById(Long snapshotId) {
        return snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new AppException(ErrorCode.SNAPSHOT_NOT_FOUND,
                        "Snapshot not found: " + snapshotId));
    }

    // ── Tier Filter Evaluation (inlined from PriorityTierFilterEvaluationService) ──

    /**
     * Evaluate xem employee có match với tier filters không.
     */
    private boolean evaluateTierFilters(PriorityTier tier, Employee employee,
                                        Map<String, Object> employeeMetrics) {
        List<PriorityTierFilter> filters = tier.getFilters();

        if (filters == null || filters.isEmpty()) {
            return true;
        }

        List<PriorityTierFilter> sortedFilters = filters.stream()
                .sorted((f1, f2) -> Integer.compare(f1.getFilterOrder(), f2.getFilterOrder()))
                .toList();

        if (tier.getFilterLogic() == FilterLogic.AND) {
            return sortedFilters.stream()
                    .allMatch(filter -> evaluateSingleFilter(filter, employeeMetrics));
        } else {
            return sortedFilters.stream()
                    .anyMatch(filter -> evaluateSingleFilter(filter, employeeMetrics));
        }
    }

    /**
     * Evaluate 1 filter.
     */
    private boolean evaluateSingleFilter(PriorityTierFilter filter, Map<String, Object> metricValues) {
        String metricName = filter.getMetricName();
        Object metricValue = metricValues.get(metricName);

        if (metricValue == null) {
            log.warn("Metric not found in calculated values: {}", metricName);
            return false;
        }

        return PriorityUtils.compareValue(metricValue, filter.getOperator(), filter.getFilterValue());
    }

    // ── Tier Ranking (inlined from PriorityTierRankingService) ──────────

    /**
     * Sắp xếp employees theo ranking metric của tier.
     */
    private List<EmployeeRankingResult> rankEmployees(PriorityTier tier, List<Employee> employees,
                                                      Map<Long, Map<String, Object>> employeesMetrics) {
        Comparator<Employee> comparator = createRankingComparator(tier, employeesMetrics);

        List<Employee> sortedEmployees = employees.stream()
                .sorted(comparator)
                .toList();

        return sortedEmployees.stream()
                .map(emp -> new EmployeeRankingResult(
                        emp,
                        employeesMetrics.get(emp.getId()),
                        sortedEmployees.indexOf(emp) + 1
                ))
                .toList();
    }

    /**
     * Tạo comparator để sắp xếp employees.
     */
    private Comparator<Employee> createRankingComparator(PriorityTier tier,
                                                         Map<Long, Map<String, Object>> employeesMetrics) {
        String rankingMetric = tier.getRankingMetric();
        RankingDirection rankingDir = tier.getRankingDirection();
        String secondaryMetric = tier.getSecondaryMetric();
        RankingDirection secondaryDir = tier.getSecondaryDirection();

        Comparator<Employee> comparator = (emp1, emp2) -> {
            Object val1 = employeesMetrics.get(emp1.getId()).get(rankingMetric);
            Object val2 = employeesMetrics.get(emp2.getId()).get(rankingMetric);

            int cmp = compareRankingValues(val1, val2);
            return rankingDir == RankingDirection.DESC ? -cmp : cmp;
        };

        if (secondaryMetric != null && !secondaryMetric.isBlank()) {
            comparator = comparator.thenComparing((emp1, emp2) -> {
                Object val1 = employeesMetrics.get(emp1.getId()).get(secondaryMetric);
                Object val2 = employeesMetrics.get(emp2.getId()).get(secondaryMetric);

                int cmp = compareRankingValues(val1, val2);
                return secondaryDir == RankingDirection.DESC ? -cmp : cmp;
            });
        }

        return comparator;
    }

    /**
     * So sánh 2 giá trị cho ranking.
     */
    private int compareRankingValues(Object val1, Object val2) {
        if (val1 == null && val2 == null) return 0;
        if (val1 == null) return -1;
        if (val2 == null) return 1;

        try {
            if (val1 instanceof Number && val2 instanceof Number) {
                BigDecimal num1 = new BigDecimal(val1.toString());
                BigDecimal num2 = new BigDecimal(val2.toString());
                return num1.compareTo(num2);
            }
            return val1.toString().compareTo(val2.toString());
        } catch (Exception e) {
            log.error("Error comparing values: {} and {}", val1, val2, e);
            return 0;
        }
    }

    // ── Inner class for ranking results ─────────────────────────────────

    /**
     * Result of employee ranking within a tier.
     */
    public static class EmployeeRankingResult {
        public final Employee employee;
        public final Map<String, Object> metricValues;
        public final int sortRank;

        public EmployeeRankingResult(Employee employee, Map<String, Object> metricValues, int sortRank) {
            this.employee = employee;
            this.metricValues = metricValues;
            this.sortRank = sortRank;
        }

        public Long getEmployeeId() {
            return employee.getId();
        }

        public String getEmployeeCode() {
            return employee.getEmployeeCode();
        }

        public String getFullName() {
            return employee.getFullName();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Set<String> collectMetricsFromPolicy(PriorityPolicy policy) {
        Set<String> metrics = new java.util.HashSet<>();
        for (PriorityTier tier : policy.getTiers()) {
            metrics.add(tier.getRankingMetric());
            if (tier.getSecondaryMetric() != null && !tier.getSecondaryMetric().isBlank()) {
                metrics.add(tier.getSecondaryMetric());
            }
            for (var filter : tier.getFilters()) {
                metrics.add(filter.getMetricName());
            }
        }
        return metrics;
    }

    private String serializePolicy(PriorityPolicy policy) {
        try {
            return objectMapper.writeValueAsString(policy);
        } catch (Exception e) {
            log.error("Error serializing policy", e);
            return "{}";
        }
    }

    private String serializeMetrics(Map<String, Object> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            log.error("Error serializing metrics", e);
            return "{}";
        }
    }
}