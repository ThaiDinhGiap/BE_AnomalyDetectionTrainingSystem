package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.dashboard.*;
import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDetailRepository trainingPlanDetailRepository;
    private final TrainingResultRepository trainingResultRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final DefectRepository defectRepository;
    private final DefectProposalRepository defectProposalRepository;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
    private final ProcessRepository processRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final TrainingSampleRepository trainingSampleRepository;
    private final UserRepository userRepository;

    // ======================== Color palette for pie charts
    // ========================
    private static final String[] PIE_COLORS = {
            "#ef4444", "#f59e0b", "#3b82f6", "#10b981", "#8b5cf6",
            "#ec4899", "#14b8a6", "#f97316", "#6366f1", "#84cc16"
    };

    // ======================== 1. KPI ========================

    @Override
    public KpiData getKpi(Long lineId, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();
        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // Get all approved training plans — filter by lineId if provided
        List<TrainingPlan> plans;
        if (lineId != null) {
            plans = trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser, lineId);
        } else {
            plans = trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser);
        }

        List<Long> approvedPlanIds = plans.stream()
                .filter(p -> (p.getStatus() == ReportStatus.APPROVED || p.getStatus() == ReportStatus.ON_GOING
                        || p.getStatus() == ReportStatus.DONE))
                .filter(p -> currentUser.equals(p.getCreatedBy()))
                .map(TrainingPlan::getId)
                .toList();

        // Plan progress: count plan details in this month
        int planTotal = 0;
        int planDone = 0;

        if (!approvedPlanIds.isEmpty()) {
            for (Long planId : approvedPlanIds) {
                List<TrainingPlanDetail> details = trainingPlanDetailRepository
                        .findByTrainingPlanIdAndDeleteFlagFalse(planId);
                for (TrainingPlanDetail d : details) {
                    if (d.getPlannedDate() != null &&
                            !d.getPlannedDate().isBefore(monthStart) &&
                            !d.getPlannedDate().isAfter(monthEnd)) {
                        planTotal++;
                        if (d.getStatus() == TrainingPlanDetailStatus.DONE) {
                            planDone++;
                        }
                    }
                }
            }
        }

        String progressPercent = planTotal > 0
                ? Math.round((double) planDone / planTotal * 100) + "%"
                : "0%";

        // Today's training count
        int todayCount = 0;
        int todayFail = 0;
        int todayMiss = 0;

        for (Long planId : approvedPlanIds) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(planId);
            for (TrainingPlanDetail d : details) {
                if (today.equals(d.getPlannedDate())) {
                    todayCount++;
                }
            }
        }

        // Failed & missed from result details (already supports null lineId)
        List<TrainingResultDetail> pendingSignatures = trainingResultDetailRepository.findPendingSignatures(lineId);
        List<TrainingResultDetail> failedTrainings = trainingResultDetailRepository.findFailedTrainings(lineId);

        // Count results that are fail for today
        for (TrainingResultDetail rd : failedTrainings) {
            if (rd.getActualDate() != null && rd.getActualDate().equals(today)) {
                todayFail++;
            }
        }

        // Count missed plan details
        for (Long planId : approvedPlanIds) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(planId);
            for (TrainingPlanDetail d : details) {
                if (d.getPlannedDate() != null && d.getPlannedDate().isBefore(today)
                        && d.getStatus() == TrainingPlanDetailStatus.PENDING) {
                    todayMiss++;
                }
            }
        }

        // Pending signatures
        int pendingSignCount = pendingSignatures.size();

        // Monthly defects — aggregate across all lines from user's plans when lineId is
        // null
        int monthlyDefects;
        if (lineId != null) {
            List<Defect> allDefects = defectRepository
                    .findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(lineId);
            monthlyDefects = (int) allDefects.stream()
                    .filter(d -> d.getDetectedDate() != null &&
                            !d.getDetectedDate().isBefore(monthStart) &&
                            !d.getDetectedDate().isAfter(monthEnd))
                    .count();
        } else {
            // Collect distinct line IDs from user's plans
            Set<Long> userLineIds = plans.stream()
                    .map(p -> p.getLine().getId())
                    .collect(Collectors.toSet());
            if (!userLineIds.isEmpty()) {
                List<Defect> allDefects = defectRepository
                        .findAllByProductLineIdsAndDeleteFlagFalse(new ArrayList<>(userLineIds));
                monthlyDefects = (int) allDefects.stream()
                        .filter(d -> d.getDetectedDate() != null &&
                                !d.getDetectedDate().isBefore(monthStart) &&
                                !d.getDetectedDate().isAfter(monthEnd))
                        .count();
            } else {
                monthlyDefects = 0;
            }
        }

        return KpiData.builder()
                .planDone(planDone)
                .planTotal(planTotal)
                .planProgressPercent(progressPercent)
                .todayTrainingCount(todayCount)
                .todayFailCount(todayFail)
                .todayMissCount(todayMiss)
                .pendingSignatureCount(pendingSignCount)
                .monthlyDefectCount(monthlyDefects)
                .build();
    }

    // ======================== Helper: resolve user line IDs
    // ========================

    /**
     * When lineId is provided, returns just that lineId.
     * When lineId is null, returns all line IDs from the user's plans.
     */
    private List<Long> resolveUserLineIds(Long lineId) {
        if (lineId != null)
            return List.of(lineId);
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser).stream()
                .map(p -> p.getLine().getId())
                .distinct()
                .toList();
    }

    // ======================== 2. Rejected Reports ========================

    @Override
    public List<RejectedReportItem> getRejectedReports(Long lineId, Integer type) {
        List<RejectedReportItem> items = new ArrayList<>();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        List<ReportStatus> rejectedStatuses = List.of(
                ReportStatus.REJECTED_BY_SV,
                ReportStatus.REJECTED_BY_MANAGER);

        // Rejected Training Plans
        if (type == null || type == 1) {
            List<TrainingPlan> plans;
            if (lineId != null) {
                plans = trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser, lineId);
            } else {
                plans = trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser);
            }
            plans.stream()
                    .filter(p -> rejectedStatuses.contains(p.getStatus()))
                    .forEach(p -> items.add(RejectedReportItem.builder()
                            .id(p.getId())
                            .type(1)
                            .title(p.getTitle() != null ? p.getTitle() : "Kế hoạch huấn luyện #" + p.getId())
                            .description(p.getNote() != null ? p.getNote() : "")
                            .entityType("TRAINING_PLAN")
                            .build()));
        }

        // Rejected Training Results
        if (type == null || type == 2) {
            List<TrainingResult> results;
            if (lineId != null) {
                results = trainingResultRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser, lineId);
            } else {
                results = trainingResultRepository.findByCreatedByAndDeleteFlagFalse(currentUser);
            }
            results.stream()
                    .filter(r -> rejectedStatuses.contains(r.getStatus()))
                    .forEach(r -> items.add(RejectedReportItem.builder()
                            .id(r.getId())
                            .type(2)
                            .title(r.getTitle() != null ? r.getTitle() : "Báo cáo kết quả #" + r.getId())
                            .description(r.getNote() != null ? r.getNote() : "")
                            .entityType("TRAINING_RESULT")
                            .build()));
        }

        // Rejected Defect Proposals
        if (type == null || type == 3) {
            List<DefectProposal> defectProposals;
            if (lineId != null) {
                defectProposals = defectProposalRepository.findByProductLineIdAndCreatedByOrderByCreatedAtDesc(lineId,
                        currentUser);
            } else {
                defectProposals = defectProposalRepository.findByDeleteFlagFalse().stream()
                        .filter(p -> currentUser.equals(p.getCreatedBy()))
                        .toList();
            }
            defectProposals.stream()
                    .filter(p -> !p.isDeleteFlag() && rejectedStatuses.contains(p.getStatus()))
                    .forEach(p -> items.add(RejectedReportItem.builder()
                            .id(p.getId())
                            .type(3)
                            .title("Đề xuất lỗi #" + p.getId())
                            .description(p.getFormCode() != null ? p.getFormCode() : "")
                            .entityType("DEFECT_PROPOSAL")
                            .build()));
        }

        // Rejected Training Sample Proposals
        if (type == null || type == 4) {
            List<TrainingSampleProposal> sampleProposals;
            if (lineId != null) {
                sampleProposals = trainingSampleProposalRepository
                        .findByProductLineIdAndCreatedByOrderByCreatedAtDesc(lineId, currentUser);
            } else {
                sampleProposals = trainingSampleProposalRepository.findByDeleteFlagFalse().stream()
                        .filter(p -> currentUser.equals(p.getCreatedBy()))
                        .toList();
            }
            sampleProposals.stream()
                    .filter(p -> !p.isDeleteFlag() && rejectedStatuses.contains(p.getStatus()))
                    .forEach(p -> items.add(RejectedReportItem.builder()
                            .id(p.getId())
                            .type(4)
                            .title("Đề xuất mẫu #" + p.getId())
                            .description(p.getFormCode() != null ? p.getFormCode() : "")
                            .entityType("TRAINING_SAMPLE_PROPOSAL")
                            .build()));
        }

        return items;
    }

    // ======================== 3. Training Tasks ========================

    @Override
    public TrainingTaskData getTrainingTasks(Long lineId) {
        LocalDate today = LocalDate.now();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Lấy danh sách active Plan
        List<TrainingPlan> plans;
        if (lineId != null) {
            plans = trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser, lineId);
        } else {
            plans = trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser);
        }
        List<Long> activePlanIds = plans.stream()
                .filter(p -> (p.getStatus() == ReportStatus.APPROVED || p.getStatus() == ReportStatus.ON_GOING))
                .map(TrainingPlan::getId)
                .toList();

        List<TrainingTaskToday> todayList = new ArrayList<>();
        List<TrainingTaskFailed> failedList = new ArrayList<>();
        List<TrainingTaskMissed> missedList = new ArrayList<>();

        if (activePlanIds.isEmpty()) {
            return TrainingTaskData.builder()
                    .todayList(todayList).failedList(failedList).missedList(missedList).build();
        }

        // 2. Lấy danh sách Detail của Plan và Result
        List<TrainingPlanDetail> allPlanDetails = new ArrayList<>();
        for (Long planId : activePlanIds) {
            allPlanDetails.addAll(trainingPlanDetailRepository.findByTrainingPlanIdAndDeleteFlagFalse(planId));
        }
        List<TrainingResultDetail> allFailedResultDetails = trainingResultDetailRepository.findFailedTrainings(lineId);

        List<TrainingResult> activeResults = trainingResultRepository.findByTrainingPlanIdIn(activePlanIds);
        Map<Long, Long> resultIdByPlanId = activeResults.stream()
                .filter(r -> r.getTrainingPlan() != null)
                .collect(Collectors.toMap(
                        r -> r.getTrainingPlan().getId(),
                        TrainingResult::getId,
                        (existing, replacement) -> existing));

        // --- Consolidated Employee ID Collection ---
        Set<Long> allEmployeeIds = new HashSet<>();
        allPlanDetails.stream()
                .filter(d -> d.getEmployee() != null)
                .map(d -> d.getEmployee().getId())
                .forEach(allEmployeeIds::add);
        allFailedResultDetails.stream()
                .filter(rd -> rd.getEmployee() != null)
                .map(rd -> rd.getEmployee().getId())
                .forEach(allEmployeeIds::add);

        Map<Long, List<String>> skillsByEmployeeId = new HashMap<>();
        if (!allEmployeeIds.isEmpty()) {
            List<EmployeeSkill> allSkills = employeeSkillRepository.findByEmployeeIdIn(new ArrayList<>(allEmployeeIds));
            skillsByEmployeeId = allSkills.stream()
                    .collect(Collectors.groupingBy(
                            skill -> skill.getEmployee().getId(),
                            Collectors.mapping(skill -> skill.getProcess().getName(), Collectors.toList())));
        }
        // --- End Consolidated Employee ID Collection ---

        // 3. Phân loại Today và Missed
        for (TrainingPlanDetail d : allPlanDetails) {
            Employee emp = d.getEmployee();
            String empName = emp != null ? emp.getFullName() : "N/A";
            String empCode = emp != null ? emp.getEmployeeCode() : "N/A";
            String employeeProcessesString = String.join(", ",
                    skillsByEmployeeId.getOrDefault(emp != null ? emp.getId() : null, Collections.emptyList()));

            Long planId = d.getTrainingPlan() != null ? d.getTrainingPlan().getId() : null;
            Long resultId = planId != null ? resultIdByPlanId.get(planId) : null; // Lấy resultId từ Map

            // Today's tasks
            if (today.equals(d.getPlannedDate()) && d.getStatus() == TrainingPlanDetailStatus.PENDING) {
                todayList.add(TrainingTaskToday.builder()
                        .id(d.getId())
                        .planId(planId)
                        .resultId(resultId)
                        .employeeName(empName)
                        .employeeCode(empCode)
                        .employeeProcesses(employeeProcessesString)
                        .timeSlot(String.valueOf(d.getPlannedDate()))
                        .build());
            }

            // Missed (past & still PENDING)
            if (d.getPlannedDate() != null && d.getPlannedDate().isBefore(today)
                    && d.getStatus() == TrainingPlanDetailStatus.PENDING) {
                missedList.add(TrainingTaskMissed.builder()
                        .id(d.getId())
                        .planId(planId)
                        .resultId(resultId)
                        .employeeName(empName)
                        .employeeCode(empCode)
                        .employeeProcesses(employeeProcessesString)
                        .date(d.getPlannedDate().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .reason("Chưa thực hiện")
                        .action("Xếp lịch lại")
                        .build());
            }
        }

        for (TrainingResultDetail rd : allFailedResultDetails) {
            if (rd.getActualDate() != null) {
                Employee emp = rd.getEmployee();

                Long parentResultId = rd.getTrainingResult() != null ? rd.getTrainingResult().getId() : null;
                Long parentPlanId = (rd.getTrainingResult() != null && rd.getTrainingResult().getTrainingPlan() != null)
                        ? rd.getTrainingResult().getTrainingPlan().getId()
                        : null;

                failedList.add(TrainingTaskFailed.builder()
                        .id(rd.getId()) // detailId
                        .planId(parentPlanId)
                        .resultId(parentResultId)
                        .employeeName(emp != null ? emp.getFullName() : "N/A")
                        .employeeCode(emp != null ? emp.getEmployeeCode() : "N/A")
                        .processName(rd.getProcess() != null
                                ? rd.getProcess().getCode() + " (" + rd.getProcess().getName() + ")"
                                : "")
                        .date(rd.getActualDate().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .reason(rd.getNote() != null ? rd.getNote() : "Không đạt thực hành")
                        .action("Đào tạo lại")
                        .build());
            }
        }

        return TrainingTaskData.builder()
                .todayList(todayList)
                .failedList(failedList)
                .missedList(missedList)
                .build();
    }

    // ======================== 4. Training Heatmap ========================

    @Override
    public Map<String, Integer> getTrainingHeatmap(Long lineId, Integer year) {
        Map<String, Integer> heatmap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<TrainingPlan> plans;
        if (lineId != null) {
            plans = trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser, lineId);
        } else {
            plans = trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser);
        }
        List<Long> planIds = plans.stream()
                .filter(p -> p.getStatus() != ReportStatus.DRAFT)
                .map(TrainingPlan::getId)
                .toList();

        // Collect all plan details and group by planned_date
        Map<LocalDate, Integer> countByDate = new HashMap<>();

        for (Long planId : planIds) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(planId);
            for (TrainingPlanDetail d : details) {
                if (d.getPlannedDate() != null && d.getPlannedDate().getYear() == targetYear) {
                    countByDate.merge(d.getPlannedDate(), 1, Integer::sum);
                }
            }
        }

        // Fill all days of the year
        LocalDate start = LocalDate.of(targetYear, 1, 1);
        LocalDate end = LocalDate.of(targetYear, 12, 31);
        LocalDate current = start;
        while (!current.isAfter(end)) {
            heatmap.put(current.format(formatter), countByDate.getOrDefault(current, 0));
            current = current.plusDays(1);
        }

        return heatmap;
    }

    // ======================== 5. Training Execution Chart ========================

    @Override
    public List<TrainingExecutionPoint> getTrainingExecution(Long lineId, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();
        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // Get all plan details for this line in this month
        List<TrainingPlan> allPlans;
        if (lineId != null) {
            allPlans = trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser, lineId);
        } else {
            allPlans = trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser);
        }
        List<TrainingPlan> plans = allPlans.stream()
                .filter(p -> p.getStatus() != ReportStatus.DRAFT)
                .toList();

        List<TrainingPlanDetail> allDetails = new ArrayList<>();

        for (TrainingPlan plan : plans) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
            for (TrainingPlanDetail d : details) {
                if (d.getPlannedDate() != null &&
                        !d.getPlannedDate().isBefore(monthStart) &&
                        !d.getPlannedDate().isAfter(monthEnd)) {
                    allDetails.add(d);
                }
            }
        }

        // Get result details for this line
        List<TrainingResultDetail> resultDetails = new ArrayList<>();
        for (TrainingPlan plan : plans) {
            List<TrainingPlanDetail> planDetails = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
            for (TrainingPlanDetail pd : planDetails) {
                List<TrainingResultDetail> rds = trainingResultDetailRepository.findByTrainingPlanDetailId(pd.getId());
                resultDetails.addAll(rds);
            }
        }

        // Build cumulative data per unique date
        Set<LocalDate> allDates = new TreeSet<>();
        allDetails.forEach(d -> allDates.add(d.getPlannedDate()));
        resultDetails.stream()
                .filter(r -> r.getActualDate() != null &&
                        !r.getActualDate().isBefore(monthStart) &&
                        !r.getActualDate().isAfter(monthEnd))
                .forEach(r -> allDates.add(r.getActualDate()));

        if (allDates.isEmpty()) {
            return List.of();
        }

        List<TrainingExecutionPoint> points = new ArrayList<>();
        int cumulativePlan = 0;
        int cumulativeActual = 0;
        int cumulativeMissed = 0;

        for (LocalDate date : allDates) {
            // Planned for this date
            long plannedOnDate = allDetails.stream()
                    .filter(d -> d.getPlannedDate().equals(date))
                    .count();
            cumulativePlan += (int) plannedOnDate;

            // Actual done on this date
            long actualOnDate = resultDetails.stream()
                    .filter(r -> r.getActualDate() != null && r.getActualDate().equals(date)
                            && Boolean.TRUE.equals(r.getIsPass()))
                    .count();
            cumulativeActual += (int) actualOnDate;

            // Missed: planned on this date but not done (only count if date has passed)
            if (!date.isAfter(today)) {
                long missedOnDate = allDetails.stream()
                        .filter(d -> d.getPlannedDate().equals(date) && d.getStatus() != TrainingPlanDetailStatus.DONE)
                        .count();
                cumulativeMissed += (int) missedOnDate;
            }

            points.add(TrainingExecutionPoint.builder()
                    .date(date.format(dateFormatter))
                    .keHoach(cumulativePlan)
                    .thucTe(cumulativeActual)
                    .biLo(cumulativeMissed)
                    .build());
        }

        return points;
    }

    // ======================== 6. Process Flow ========================

    @Override
    public List<ProcessFlowItem> getProcessFlow(Long lineId) {
        List<Process> processes;
        if (lineId != null) {
            processes = processRepository.findByProductLineIdAndDeleteFlagFalse(lineId);
        } else {
            List<Long> userLineIds = resolveUserLineIds(null);
            processes = userLineIds.isEmpty() ? List.of()
                    : processRepository.findByProductLineIdInAndDeleteFlagFalse(userLineIds);
        }
        return processes.stream()
                .sorted(Comparator.comparing(Process::getCode))
                .map(p -> ProcessFlowItem.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .classification(p.getClassification().getValue())
                        .build())
                .toList();
    }

    // ======================== 7. Skill Certificates ========================

    @Override
    public List<SkillCertificateItem> getSkillCertificates(Long lineId) {
        List<Process> processes;
        if (lineId != null) {
            processes = processRepository.findByProductLineIdAndDeleteFlagFalse(lineId);
        } else {
            List<Long> userLineIds = resolveUserLineIds(null);
            processes = userLineIds.isEmpty() ? List.of()
                    : processRepository.findByProductLineIdInAndDeleteFlagFalse(userLineIds);
        }

        return processes.stream()
                .sorted(Comparator.comparing(Process::getCode))
                .map(process -> {
                    List<EmployeeSkill> skills = employeeSkillRepository
                            .findByProcessIdAndDeleteFlagFalse(process.getId());

                    List<String> validNames = new ArrayList<>();
                    List<String> expiringNames = new ArrayList<>();
                    List<String> revokedNames = new ArrayList<>();

                    for (EmployeeSkill skill : skills) {
                        String empName = skill.getEmployee() != null ? skill.getEmployee().getFullName() : "N/A";

                        if (skill.getStatus() == EmployeeSkillStatus.VALID) {
                            validNames.add(empName);
                        } else if (skill.getStatus() == EmployeeSkillStatus.PENDING_REVIEW) {
                            String suffix = skill.getExpiryDate() != null
                                    ? " (" + skill.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM")) + ")"
                                    : "";
                            expiringNames.add(empName + suffix);
                        } else if (skill.getStatus() == EmployeeSkillStatus.REVOKED) {
                            revokedNames.add(empName + " (Đã thu hồi)");
                        }
                    }

                    return SkillCertificateItem.builder()
                            .process(process.getCode() + " (" + process.getName() + ")")
                            .valid(validNames.size())
                            .validNames(validNames)
                            .expiring(expiringNames.size())
                            .expiringNames(expiringNames)
                            .revoked(revokedNames.size())
                            .revokedNames(revokedNames)
                            .build();
                })
                .toList();
    }

    // ======================== 8. Defect Trend ========================

    @Override
    public List<DefectTrendPoint> getDefectTrend(Long lineId) {
        List<Defect> allDefects;
        if (lineId != null) {
            allDefects = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(lineId);
        } else {
            List<Long> userLineIds = resolveUserLineIds(null);
            allDefects = userLineIds.isEmpty() ? List.of()
                    : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(userLineIds);
        }

        // Group by detected_date, last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        Map<LocalDate, List<Defect>> byDate = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null && !d.getDetectedDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.groupingBy(Defect::getDetectedDate, TreeMap::new, Collectors.toList()));

        return byDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Defect> defects = entry.getValue();

                    // Group errors by description and process
                    Map<String, List<Defect>> byDescription = defects.stream()
                            .collect(Collectors.groupingBy(
                                    d -> d.getDefectDescription() + "|" +
                                            (d.getProcess() != null ? d.getProcess().getName() : "N/A")));

                    List<DefectErrorDetail> errors = byDescription.entrySet().stream()
                            .map(e -> {
                                String[] parts = e.getKey().split("\\|", 2);
                                return DefectErrorDetail.builder()
                                        .content(parts[0])
                                        .process(parts.length > 1 ? parts[1] : "N/A")
                                        .quantity(e.getValue().size())
                                        .build();
                            })
                            .toList();

                    return DefectTrendPoint.builder()
                            .date(date.format(formatter))
                            .totalErrors(defects.size())
                            .errors(errors)
                            .build();
                })
                .toList();
    }

    // ======================== 9. Defect by Process ========================

    @Override
    public List<StageDistribution> getDefectByProcess(Long lineId) {
        List<Defect> allDefects;
        if (lineId != null) {
            allDefects = defectRepository.findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(lineId);
        } else {
            List<Long> userLineIds = resolveUserLineIds(null);
            allDefects = userLineIds.isEmpty() ? List.of()
                    : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(userLineIds);
        }

        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (Defect d : allDefects) {
            String processName = d.getProcess() != null ? d.getProcess().getName() : "Khác";
            countByProcess.merge(processName, 1, Integer::sum);
        }

        int colorIndex = 0;
        List<StageDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByProcess.entrySet()) {
            result.add(StageDistribution.builder()
                    .stage(entry.getKey())
                    .value(entry.getValue())
                    .color(PIE_COLORS[colorIndex % PIE_COLORS.length])
                    .build());
            colorIndex++;
        }

        return result;
    }

    // ======================== 10. Training Samples by Process
    // ========================

    @Override
    public List<StageDistribution> getSampleByProcess(Long lineId) {
        List<TrainingSample> allSamples;
        if (lineId != null) {
            allSamples = trainingSampleRepository.findByProductLineIdAndDeleteFlagFalseOrderByCreatedAtDesc(lineId);
        } else {
            List<Long> userLineIds = resolveUserLineIds(null);
            allSamples = userLineIds.isEmpty() ? List.of()
                    : trainingSampleRepository.findByProductLineIdInAndDeleteFlagFalse(userLineIds);
        }

        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (TrainingSample s : allSamples) {
            String processName = s.getProcess() != null ? s.getProcess().getName() : "Khác";
            countByProcess.merge(processName, 1, Integer::sum);
        }

        int colorIndex = 0;
        List<StageDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByProcess.entrySet()) {
            result.add(StageDistribution.builder()
                    .stage(entry.getKey())
                    .value(entry.getValue())
                    .color(PIE_COLORS[colorIndex % PIE_COLORS.length])
                    .build());
            colorIndex++;
        }

        return result;
    }

    // ======================== SV Dashboard ========================

    private final ProductLineRepository productLineRepository;
    private final TeamRepository teamRepository;
    private final GroupRepository groupRepository;
    private final SectionRepository sectionRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Resolve danh sách groupIds theo supervisorId.
     * Khi groupId != null → trả về [groupId].
     * Khi groupId == null → lấy tất cả group mà SV quản lý.
     */
    private List<Long> resolveSvGroupIds(Long groupId) {
        if (groupId != null)
            return List.of(groupId);
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUser).orElse(null);
        if (user == null)
            return List.of();
        return groupRepository.findBySupervisorId(user.getId()).stream()
                .map(Group::getId).toList();
    }

    /**
     * Resolve danh sách lineIds theo groupId + optional lineId.
     */
    private List<Long> resolveLineIds(Long groupId, Long lineId) {
        if (lineId != null) {
            return List.of(lineId);
        }
        List<Long> groupIds = resolveSvGroupIds(groupId);
        if (groupIds.isEmpty())
            return List.of();
        return groupIds.stream()
                .flatMap(gId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(gId).stream())
                .map(ProductLine::getId)
                .distinct()
                .toList();
    }

    // ======================== SV-1. Lines for dropdown ========================

    @Override
    public List<SvDashboardLineResponse> getSvLines(Long groupId) {
        List<Long> groupIds = resolveSvGroupIds(groupId);
        return groupIds.stream()
                .flatMap(gId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(gId).stream())
                .map(pl -> SvDashboardLineResponse.builder()
                        .id(pl.getId())
                        .code(pl.getCode())
                        .name(pl.getName())
                        .build())
                .toList();
    }

    // ======================== SV-2. Todo List ========================

    @Override
    public SvTodoData getSvTodo(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Long> groupIds = resolveSvGroupIds(groupId);
        List<SvTodoItem> pendingItems = new ArrayList<>();

        // --- Pass Rate: calculated from TrainingResultDetail with actualDate ---
        long totalEvaluated = 0;
        long totalPassed = 0;

        List<TrainingResult> allResults = trainingResultRepository.findByGroupIds(groupIds);
        List<TrainingResult> filteredResults = allResults.stream()
                .filter(r -> lineIds.contains(r.getLine().getId()))
                .toList();

        for (TrainingResult result : filteredResults) {
            List<TrainingResultDetail> details = trainingResultDetailRepository
                    .findByTrainingResultId(result.getId());
            for (TrainingResultDetail d : details) {
                if (d.getActualDate() != null && d.getIsPass() != null) {
                    totalEvaluated++;
                    if (Boolean.TRUE.equals(d.getIsPass())) {
                        totalPassed++;
                    }
                }
            }
        }
        String passRate = totalEvaluated > 0
                ? Math.round((double) totalPassed / totalEvaluated * 1000.0) / 10.0 + "%"
                : "0%";

        // --- Participation Rate: planned details with actual training ---
        long totalPlanned = 0;
        long totalParticipated = 0;

        List<TrainingPlan> approvedPlans = trainingPlanRepository.findByGroupIds(groupIds).stream()
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> p.getStatus() == ReportStatus.APPROVED
                        || p.getStatus() == ReportStatus.ON_GOING
                        || p.getStatus() == ReportStatus.DONE)
                .toList();

        for (TrainingPlan plan : approvedPlans) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
            totalPlanned += details.size();
            for (TrainingPlanDetail d : details) {
                if (d.getStatus() == TrainingPlanDetailStatus.DONE) {
                    totalParticipated++;
                }
            }
        }
        String participationRate = totalPlanned > 0
                ? Math.round((double) totalParticipated / totalPlanned * 1000.0) / 10.0 + "%"
                : "0%";

        // --- Pending Approval: plans/results with status WAITING_SV ---
        List<TrainingPlan> waitingPlans = trainingPlanRepository.findByGroupIds(groupIds).stream()
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> p.getStatus() == ReportStatus.WAITING_SV)
                .toList();

        for (TrainingPlan plan : waitingPlans) {
            pendingItems.add(SvTodoItem.builder()
                    .id(plan.getId())
                    .entityType("TRAINING_PLAN")
                    .title("Kế hoạch đào tạo: " + (plan.getTitle() != null ? plan.getTitle() : "#" + plan.getId()))
                    .senderName(plan.getCreatedBy() != null ? plan.getCreatedBy() : "N/A")
                    .waitTime(formatWaitTime(plan.getUpdatedAt()))
                    .status("WAITING_SV")
                    .build());
        }

        List<TrainingResult> waitingResults = trainingResultRepository.findByGroupIds(groupIds).stream()
                .filter(r -> lineIds.contains(r.getLine().getId()))
                .filter(r -> r.getStatus() == ReportStatus.WAITING_SV)
                .toList();

        for (TrainingResult result : waitingResults) {
            pendingItems.add(SvTodoItem.builder()
                    .id(result.getId())
                    .entityType("TRAINING_RESULT")
                    .title("Báo cáo kết quả: " + (result.getTitle() != null ? result.getTitle() : "#" + result.getId()))
                    .senderName(result.getCreatedBy() != null ? result.getCreatedBy() : "N/A")
                    .waitTime(formatWaitTime(result.getUpdatedAt()))
                    .status("WAITING_SV")
                    .build());
        }

        return SvTodoData.builder()
                .passRate(passRate)
                .participationRate(participationRate)
                .pendingCount(pendingItems.size())
                .pendingItems(pendingItems)
                .build();
    }

    private String formatWaitTime(java.time.LocalDateTime updatedAt) {
        if (updatedAt == null)
            return "N/A";
        java.time.Duration duration = java.time.Duration.between(updatedAt, java.time.LocalDateTime.now());
        long hours = duration.toHours();
        if (hours < 1)
            return duration.toMinutes() + " phút";
        if (hours < 24)
            return hours + " giờ";
        long days = duration.toDays();
        return days + " ngày";
    }

    // ======================== SV-3. Team Benchmark ========================

    @Override
    public List<SvTeamBenchmark> getSvTeamBenchmark(Long groupId, Long lineId, Integer month, Integer year) {
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();
        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();

        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Long> groupIds = resolveSvGroupIds(groupId);

        // Get all teams in this group(s)
        List<Team> teams = teamRepository.findByGroupIdIn(groupIds);

        List<SvTeamBenchmark> benchmarks = new ArrayList<>();

        for (Team team : teams) {
            // Plans belonging to this team within the filtered lines
            List<TrainingPlan> teamPlans = trainingPlanRepository.findByTeamIdAndDeleteFlagFalse(team.getId())
                    .stream()
                    .filter(p -> lineIds.contains(p.getLine().getId()))
                    .filter(p -> p.getStatus() == ReportStatus.APPROVED
                            || p.getStatus() == ReportStatus.ON_GOING
                            || p.getStatus() == ReportStatus.DONE)
                    .toList();

            int planTotal = 0;
            int planDone = 0;

            for (TrainingPlan plan : teamPlans) {
                List<TrainingPlanDetail> details = trainingPlanDetailRepository
                        .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
                for (TrainingPlanDetail d : details) {
                    if (d.getPlannedDate() != null
                            && !d.getPlannedDate().isBefore(monthStart)
                            && !d.getPlannedDate().isAfter(monthEnd)) {
                        planTotal++;
                        if (d.getStatus() == TrainingPlanDetailStatus.DONE) {
                            planDone++;
                        }
                    }
                }
            }

            int completion = planTotal > 0 ? (int) Math.round((double) planDone / planTotal * 100) : 0;

            // Count defects for this team's lines in this month
            long defectCount = 0;
            for (TrainingPlan plan : teamPlans) {
                List<Defect> defects = defectRepository
                        .findAllByProductLineAndDeleteFlagFalseOrderByCreatedAtDesc(plan.getLine().getId());
                defectCount += defects.stream()
                        .filter(d -> d.getDetectedDate() != null
                                && !d.getDetectedDate().isBefore(monthStart)
                                && !d.getDetectedDate().isAfter(monthEnd))
                        .count();
            }

            String defectsLevel;
            if (defectCount > 10) {
                defectsLevel = "Cao";
            } else if (defectCount > 5) {
                defectsLevel = "TB";
            } else {
                defectsLevel = "Thấp";
            }

            String grade;
            if (completion >= 85) {
                grade = "Tốt";
            } else if (completion >= 70) {
                grade = "Khá";
            } else {
                grade = "Cần nhắc";
            }

            benchmarks.add(SvTeamBenchmark.builder()
                    .teamId(team.getId())
                    .name(team.getName())
                    .completion(completion)
                    .defects(defectsLevel)
                    .grade(grade)
                    .build());
        }

        return benchmarks;
    }

    // ======================== SV-4. Defect by Operation ========================

    @Override
    public List<SvDefectByOperation> getSvDefectByOperation(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Defect> allDefects = defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        // Group by process code only (not double-counting)
        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (Defect d : allDefects) {
            String label = d.getProcess() != null
                    ? d.getProcess().getCode() + " (" + d.getProcess().getName() + ")"
                    : "Khác";
            countByProcess.merge(label, 1, Integer::sum);
        }

        return countByProcess.entrySet().stream()
                .map(entry -> SvDefectByOperation.builder()
                        .op(entry.getKey())
                        .errors(entry.getValue())
                        .build())
                .sorted((a, b) -> b.getErrors() - a.getErrors())
                .toList();
    }

    // ======================== SV-5. Defect Hotspot ========================

    @Override
    public List<SvDefectHotspot> getSvDefectHotspot(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Defect> allDefects = defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        // Group by process → { processCode + processName + lineName : count }
        record ProcessLineKey(String processCode, String processName, String lineName) {
        }
        Map<ProcessLineKey, Integer> countMap = new LinkedHashMap<>();

        for (Defect d : allDefects) {
            if (d.getProcess() != null) {
                String pCode = d.getProcess().getCode();
                String pName = d.getProcess().getName();
                String lName = d.getProcess().getProductLine() != null
                        ? d.getProcess().getProductLine().getName()
                        : "N/A";
                countMap.merge(new ProcessLineKey(pCode, pName, lName), 1, Integer::sum);
            }
        }

        // Sort desc by count, take top 5
        List<Map.Entry<ProcessLineKey, Integer>> sorted = countMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .toList();

        List<SvDefectHotspot> hotspots = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<ProcessLineKey, Integer> entry : sorted) {
            ProcessLineKey key = entry.getKey();
            int count = entry.getValue();
            hotspots.add(SvDefectHotspot.builder()
                    .rank(rank++)
                    .title(key.processCode() + " (" + key.processName() + ")")
                    .line(key.lineName())
                    .count(count)
                    .danger(count > 10)
                    .build());
        }

        return hotspots;
    }

    // ======================== SV-6. KPI Cards ========================

    @Override
    public SvKpiData getSvKpi(Long groupId, Long lineId, Integer year, Integer month) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();
        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();

        // Previous month for comparison
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate prevMonthEnd = YearMonth.of(prevMonthStart.getYear(), prevMonthStart.getMonthValue()).atEndOfMonth();

        // --- 1. Pass Rate ---
        long currentPassed = 0, currentEvaluated = 0;
        long prevPassed = 0, prevEvaluated = 0;

        List<Long> groupIds = resolveSvGroupIds(groupId);
        List<TrainingResult> allResults = trainingResultRepository.findByGroupIds(groupIds).stream()
                .filter(r -> lineIds.contains(r.getLine().getId()))
                .toList();

        for (TrainingResult result : allResults) {
            List<TrainingResultDetail> details = trainingResultDetailRepository
                    .findByTrainingResultId(result.getId());
            for (TrainingResultDetail d : details) {
                if (d.getActualDate() != null && d.getIsPass() != null) {
                    if (!d.getActualDate().isBefore(monthStart) && !d.getActualDate().isAfter(monthEnd)) {
                        currentEvaluated++;
                        if (Boolean.TRUE.equals(d.getIsPass()))
                            currentPassed++;
                    }
                    if (!d.getActualDate().isBefore(prevMonthStart) && !d.getActualDate().isAfter(prevMonthEnd)) {
                        prevEvaluated++;
                        if (Boolean.TRUE.equals(d.getIsPass()))
                            prevPassed++;
                    }
                }
            }
        }

        double currentPassPct = currentEvaluated > 0 ? (double) currentPassed / currentEvaluated * 100 : 0;
        double prevPassPct = prevEvaluated > 0 ? (double) prevPassed / prevEvaluated * 100 : 0;
        double passRateDiff = Math.round((currentPassPct - prevPassPct) * 10.0) / 10.0;
        String passRateStr = Math.round(currentPassPct * 10.0) / 10.0 + "%";
        String passRateSub = (passRateDiff >= 0 ? "+" : "") + passRateDiff + "% so với tháng trước";

        // --- 2. Defect Count ---
        List<Defect> allDefects = defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);
        long currentDefects = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null
                        && !d.getDetectedDate().isBefore(monthStart)
                        && !d.getDetectedDate().isAfter(monthEnd))
                .count();
        long prevDefects = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null
                        && !d.getDetectedDate().isBefore(prevMonthStart)
                        && !d.getDetectedDate().isAfter(prevMonthEnd))
                .count();
        double defectDiff = prevDefects > 0
                ? Math.round((double) (currentDefects - prevDefects) / prevDefects * 1000.0) / 10.0
                : 0;
        String defectSub = (defectDiff >= 0 ? "+" : "") + defectDiff + "% so với tháng trước";

        // --- 3. Training Coverage ---
        List<Team> teams = teamRepository.findByGroupIdIn(resolveSvGroupIds(groupId));
        long totalEmployees = 0;
        long employeesWithValidSkill = 0;

        for (Team team : teams) {
            if (team.getEmployees() != null) {
                for (Employee emp : team.getEmployees()) {
                    if (emp.isDeleteFlag())
                        continue;
                    totalEmployees++;
                    List<EmployeeSkill> skills = employeeSkillRepository
                            .findByEmployeeIdAndDeleteFlagFalse(emp.getId());
                    boolean hasValid = skills.stream()
                            .anyMatch(s -> s.getStatus() == EmployeeSkillStatus.VALID);
                    if (hasValid)
                        employeesWithValidSkill++;
                }
            }
        }
        double coveragePct = totalEmployees > 0
                ? Math.round((double) employeesWithValidSkill / totalEmployees * 1000.0) / 10.0
                : 0;

        // --- 4. Pending Approval Count ---
        long pendingPlans = trainingPlanRepository.findByGroupIds(groupIds).stream()
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> p.getStatus() == ReportStatus.WAITING_SV)
                .count();
        long pendingResults = trainingResultRepository.findByGroupIds(groupIds).stream()
                .filter(r -> lineIds.contains(r.getLine().getId()))
                .filter(r -> r.getStatus() == ReportStatus.WAITING_SV)
                .count();
        int totalPending = (int) (pendingPlans + pendingResults);

        return SvKpiData.builder()
                .passRate(passRateStr)
                .passRateSub(passRateSub)
                .defectCount((int) currentDefects)
                .defectSub(defectSub)
                .trainingCoverage(coveragePct + "%")
                .coverageSub("Nhân viên có chứng chỉ hợp lệ")
                .pendingApprovalCount(totalPending)
                .pendingSub(totalPending > 0 ? "Hồ sơ đang chờ phê duyệt" : "Không có hồ sơ chờ duyệt")
                .build();
    }

    // ======================== SV-7. Watchlist ========================

    @Override
    public List<SvWatchlistItem> getSvWatchlist(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Team> teams = teamRepository.findByGroupIdIn(resolveSvGroupIds(groupId));
        List<SvWatchlistItem> items = new ArrayList<>();

        for (Team team : teams) {
            if (team.getEmployees() == null)
                continue;
            for (Employee emp : team.getEmployees()) {
                if (emp.isDeleteFlag())
                    continue;
                List<EmployeeSkill> skills = employeeSkillRepository
                        .findByEmployeeIdAndDeleteFlagFalse(emp.getId());

                for (EmployeeSkill skill : skills) {
                    if (skill.getProcess() == null)
                        continue;
                    // Only include skills related to selected lines
                    if (!lineIds.contains(skill.getProcess().getProductLine().getId()))
                        continue;

                    if (skill.getStatus() == EmployeeSkillStatus.REVOKED
                            || skill.getStatus() == EmployeeSkillStatus.PENDING_REVIEW) {

                        String statusText = skill.getStatus() == EmployeeSkillStatus.REVOKED
                                ? "Fail"
                                : "Cần giám sát";

                        String reason;
                        if (skill.getStatus() == EmployeeSkillStatus.REVOKED) {
                            reason = "Chứng chỉ đã bị thu hồi";
                        } else {
                            reason = "Chứng chỉ cần đánh giá lại";
                        }

                        items.add(SvWatchlistItem.builder()
                                .id(skill.getId())
                                .name(emp.getFullName())
                                .empId(emp.getEmployeeCode())
                                .role(skill.getProcess().getName() + " • "
                                        + skill.getProcess().getProductLine().getName()
                                        + " (" + skill.getProcess().getCode() + ")")
                                .status(statusText)
                                .reason(reason)
                                .build());
                    }
                }
            }
        }
        return items;
    }

    // ======================== SV-8. Recent Activity ========================

    @Override
    public List<SvRecentActivityItem> getSvRecentActivity(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<SvRecentActivityItem> items = new ArrayList<>();

        // Get recent training evaluations from all results in this group
        List<TrainingResult> results = trainingResultRepository.findByGroupIds(resolveSvGroupIds(groupId)).stream()
                .filter(r -> lineIds.contains(r.getLine().getId()))
                .toList();

        for (TrainingResult result : results) {
            List<TrainingResultDetail> details = trainingResultDetailRepository
                    .findByTrainingResultId(result.getId());
            for (TrainingResultDetail d : details) {
                if (d.getIsPass() != null && d.getActualDate() != null) {
                    String empName = "N/A";
                    if (d.getTrainingPlanDetail() != null && d.getTrainingPlanDetail().getEmployee() != null) {
                        empName = d.getTrainingPlanDetail().getEmployee().getFullName();
                    } else if (d.getEmployee() != null) {
                        empName = d.getEmployee().getFullName();
                    }

                    String processName = d.getProcess() != null
                            ? "HL " + d.getProcess().getName() + " " + d.getProcess().getCode()
                            : "N/A";

                    items.add(SvRecentActivityItem.builder()
                            .id(d.getId())
                            .name(empName)
                            .action(Boolean.TRUE.equals(d.getIsPass())
                                    ? "Đánh giá Đạt"
                                    : "Đánh giá Không đạt")
                            .model(processName)
                            .time(formatWaitTime(d.getUpdatedAt()))
                            .status(Boolean.TRUE.equals(d.getIsPass()) ? "success" : "fail")
                            .build());
                }
            }
        }

        // Sort by most recent and limit to 20
        items.sort((a, b) -> 0); // already sorted by result order
        if (items.size() > 20) {
            items = items.subList(0, 20);
        }

        return items;
    }

    // ======================== SV-9. Training Status (Donut)
    // ========================

    @Override
    public List<SvTrainingStatusItem> getSvTrainingStatus(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        LocalDate today = LocalDate.now();
        // Current week: Monday to Sunday
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(java.time.DayOfWeek.SUNDAY);

        int scheduled = 0;
        int completed = 0;
        int overdue = 0;

        List<TrainingPlan> plans = trainingPlanRepository.findByGroupIds(resolveSvGroupIds(groupId)).stream()
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> p.getStatus() == ReportStatus.APPROVED
                        || p.getStatus() == ReportStatus.ON_GOING
                        || p.getStatus() == ReportStatus.DONE)
                .toList();

        for (TrainingPlan plan : plans) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
            for (TrainingPlanDetail d : details) {
                if (d.getPlannedDate() == null)
                    continue;

                // In current week range
                if (!d.getPlannedDate().isBefore(weekStart) && !d.getPlannedDate().isAfter(weekEnd)) {
                    if (d.getStatus() == TrainingPlanDetailStatus.DONE) {
                        completed++;
                    } else {
                        scheduled++;
                    }
                }
                // Overdue: pending and planned date before today
                if (d.getStatus() == TrainingPlanDetailStatus.PENDING
                        && d.getPlannedDate().isBefore(today)) {
                    overdue++;
                }
            }
        }

        List<SvTrainingStatusItem> items = new ArrayList<>();
        items.add(SvTrainingStatusItem.builder()
                .name("Đã lên lịch huấn luyện").value(scheduled).color("#3b82f6").build());
        items.add(SvTrainingStatusItem.builder()
                .name("Đã huấn luyện").value(completed).color("#22c55e").build());
        items.add(SvTrainingStatusItem.builder()
                .name("Trễ lịch huấn luyện").value(overdue).color("#f43f5e").build());

        return items;
    }

    // ======================== SV-10. Training Effectiveness
    // ========================

    @Override
    public List<SvTrainingEffectivenessPoint> getSvTrainingEffectiveness(Long groupId, Long lineId, Integer months) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        int numMonths = months != null ? months : 6;
        LocalDate today = LocalDate.now();

        // Collect all result details and defects once
        List<TrainingResult> allResults = trainingResultRepository.findByGroupIds(resolveSvGroupIds(groupId)).stream()
                .filter(r -> lineIds.contains(r.getLine().getId()))
                .toList();

        List<TrainingResultDetail> allDetails = new ArrayList<>();
        for (TrainingResult result : allResults) {
            allDetails.addAll(trainingResultDetailRepository.findByTrainingResultId(result.getId()));
        }

        List<Defect> allDefects = defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        List<SvTrainingEffectivenessPoint> points = new ArrayList<>();

        for (int i = numMonths - 1; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            LocalDate monthStart = LocalDate.of(monthDate.getYear(), monthDate.getMonthValue(), 1);
            LocalDate monthEnd = YearMonth.of(monthDate.getYear(), monthDate.getMonthValue()).atEndOfMonth();

            int trainingCount = (int) allDetails.stream()
                    .filter(d -> d.getActualDate() != null
                            && !d.getActualDate().isBefore(monthStart)
                            && !d.getActualDate().isAfter(monthEnd))
                    .count();

            int defectCount = (int) allDefects.stream()
                    .filter(d -> d.getDetectedDate() != null
                            && !d.getDetectedDate().isBefore(monthStart)
                            && !d.getDetectedDate().isAfter(monthEnd))
                    .count();

            points.add(SvTrainingEffectivenessPoint.builder()
                    .month(monthNames[monthDate.getMonthValue() - 1])
                    .trainingHours(trainingCount)
                    .defects(defectCount)
                    .build());
        }

        return points;
    }

    // ======================== SV-11. Top Training Samples ========================

    @Override
    public List<SvTopTrainingSampleItem> getSvTopTrainingSamples(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);

        // Get all training samples for the resolved lines
        List<TrainingSample> allSamples = trainingSampleRepository.findByProductLineIdInAndDeleteFlagFalse(lineIds);
        if (allSamples.isEmpty())
            return List.of();

        // Get all result details from this group's results
        List<Long> groupIds = resolveSvGroupIds(groupId);
        List<TrainingResult> allResults = new ArrayList<>();
        for (Long gId : groupIds) {
            allResults.addAll(trainingResultRepository.findByGroupId(gId));
        }
        allResults = allResults.stream()
                .filter(r -> lineIds.contains(r.getLine().getId()) && !r.isDeleteFlag())
                .toList();

        List<TrainingResultDetail> allDetails = new ArrayList<>();
        for (TrainingResult result : allResults) {
            allDetails.addAll(trainingResultDetailRepository.findByTrainingResultId(result.getId()));
        }

        // Group by sample categoryName (or trainingCode): count used + failed
        record SampleStats(int total, int failed) {
        }
        Map<String, SampleStats> statsMap = new LinkedHashMap<>();

        // Approach 1: Group result details by trainingSample or process name
        for (TrainingResultDetail d : allDetails) {
            if (d.getIsPass() == null)
                continue;

            String sampleName;
            if (d.getTrainingSample() != null) {
                // Best case: direct link to TrainingSample
                TrainingSample ts = d.getTrainingSample();
                sampleName = ts.getCategoryName() + " (" + ts.getTrainingCode() + ")";
            } else if (d.getProcess() != null) {
                sampleName = d.getProcess().getName() + " (" + d.getProcess().getCode() + ")";
            } else if (d.getTrainingPlanDetail() != null
                    && d.getTrainingPlanDetail().getTrainingPlan() != null
                    && d.getTrainingPlanDetail().getTrainingPlan().getLine() != null) {
                // Fallback: use line name
                sampleName = d.getTrainingPlanDetail().getTrainingPlan().getLine().getName();
            } else {
                continue;
            }

            SampleStats existing = statsMap.getOrDefault(sampleName, new SampleStats(0, 0));
            int newTotal = existing.total() + 1;
            int newFailed = existing.failed() + (Boolean.TRUE.equals(d.getIsPass()) ? 0 : 1);
            statsMap.put(sampleName, new SampleStats(newTotal, newFailed));
        }

        // If no result detail data, fall back to showing samples with 0 usage
        if (statsMap.isEmpty()) {
            return allSamples.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getProcess() != null
                                    ? s.getProcess().getCode()
                                    : s.getCategoryName(),
                            Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> SvTopTrainingSampleItem.builder()
                            .name(e.getKey())
                            .used(e.getValue().intValue())
                            .failRate(0)
                            .build())
                    .sorted((a, b) -> b.getUsed() - a.getUsed())
                    .limit(5)
                    .toList();
        }

        return statsMap.entrySet().stream()
                .map(entry -> {
                    SampleStats stats = entry.getValue();
                    double failRate = stats.total() > 0
                            ? Math.round((double) stats.failed() / stats.total() * 1000.0) / 10.0
                            : 0;
                    return SvTopTrainingSampleItem.builder()
                            .name(entry.getKey())
                            .used(stats.total())
                            .failRate(failRate)
                            .build();
                })
                .sorted((a, b) -> b.getUsed() - a.getUsed())
                .limit(5)
                .toList();
    }

    // ======================== SV-12. Training Execution Chart
    // ========================

    @Override
    public List<TrainingExecutionPoint> getSvTrainingExecution(Long groupId, Long lineId, Integer year, Integer month) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();
        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        // Get all plans in resolved groups' lines (not DRAFT)
        List<Long> groupIds = resolveSvGroupIds(groupId);
        List<TrainingPlan> plans = new ArrayList<>();
        for (Long gId : groupIds) {
            plans.addAll(trainingPlanRepository.findByGroupId(gId));
        }
        plans = plans.stream()
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> !p.isDeleteFlag())
                .filter(p -> p.getStatus() != ReportStatus.DRAFT)
                .toList();

        // Collect plan details within target month
        List<TrainingPlanDetail> allDetails = new ArrayList<>();
        for (TrainingPlan plan : plans) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
            for (TrainingPlanDetail d : details) {
                if (d.getPlannedDate() != null
                        && !d.getPlannedDate().isBefore(monthStart)
                        && !d.getPlannedDate().isAfter(monthEnd)) {
                    allDetails.add(d);
                }
            }
        }

        // Collect result details
        List<TrainingResultDetail> resultDetails = new ArrayList<>();
        for (TrainingPlan plan : plans) {
            List<TrainingPlanDetail> planDetails = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(plan.getId());
            for (TrainingPlanDetail pd : planDetails) {
                List<TrainingResultDetail> rds = trainingResultDetailRepository
                        .findByTrainingPlanDetailId(pd.getId());
                resultDetails.addAll(rds);
            }
        }

        // Build cumulative data per unique date
        Set<LocalDate> allDates = new TreeSet<>();
        allDetails.forEach(d -> allDates.add(d.getPlannedDate()));
        resultDetails.stream()
                .filter(r -> r.getActualDate() != null
                        && !r.getActualDate().isBefore(monthStart)
                        && !r.getActualDate().isAfter(monthEnd))
                .forEach(r -> allDates.add(r.getActualDate()));

        if (allDates.isEmpty()) {
            return List.of();
        }

        List<TrainingExecutionPoint> points = new ArrayList<>();
        int cumulativePlan = 0;
        int cumulativeActual = 0;
        int cumulativeMissed = 0;

        for (LocalDate date : allDates) {
            // Planned for this date
            long plannedOnDate = allDetails.stream()
                    .filter(d -> d.getPlannedDate().equals(date))
                    .count();
            cumulativePlan += (int) plannedOnDate;

            // Actual done on this date
            long actualOnDate = resultDetails.stream()
                    .filter(r -> r.getActualDate() != null && r.getActualDate().equals(date)
                            && Boolean.TRUE.equals(r.getIsPass()))
                    .count();
            cumulativeActual += (int) actualOnDate;

            // Missed: planned on this date but not done (only count if date has passed)
            if (!date.isAfter(today)) {
                long missedOnDate = allDetails.stream()
                        .filter(d -> d.getPlannedDate().equals(date) && d.getStatus() != TrainingPlanDetailStatus.DONE)
                        .count();
                cumulativeMissed += (int) missedOnDate;
            }

            points.add(TrainingExecutionPoint.builder()
                    .date(date.format(dateFormatter))
                    .keHoach(cumulativePlan)
                    .thucTe(cumulativeActual)
                    .biLo(cumulativeMissed)
                    .build());
        }

        return points;
    }

    // ======================== SV-13. Skill Certificates ========================

    @Override
    public List<SkillCertificateItem> getSvSkillCertificates(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Process> processes = lineIds.isEmpty() ? List.of()
                : processRepository.findByProductLineIdInAndDeleteFlagFalse(lineIds);

        return processes.stream()
                .sorted(Comparator.comparing(Process::getCode))
                .map(process -> {
                    List<EmployeeSkill> skills = employeeSkillRepository
                            .findByProcessIdAndDeleteFlagFalse(process.getId());

                    List<String> validNames = new ArrayList<>();
                    List<String> expiringNames = new ArrayList<>();
                    List<String> revokedNames = new ArrayList<>();

                    for (EmployeeSkill skill : skills) {
                        String empName = skill.getEmployee() != null ? skill.getEmployee().getFullName() : "N/A";

                        if (skill.getStatus() == EmployeeSkillStatus.VALID) {
                            validNames.add(empName);
                        } else if (skill.getStatus() == EmployeeSkillStatus.PENDING_REVIEW) {
                            String suffix = skill.getExpiryDate() != null
                                    ? " (" + skill.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM")) + ")"
                                    : "";
                            expiringNames.add(empName + suffix);
                        } else if (skill.getStatus() == EmployeeSkillStatus.REVOKED) {
                            revokedNames.add(empName + " (Đã thu hồi)");
                        }
                    }

                    return SkillCertificateItem.builder()
                            .process(process.getCode() + " (" + process.getName() + ")")
                            .valid(validNames.size())
                            .validNames(validNames)
                            .expiring(expiringNames.size())
                            .expiringNames(expiringNames)
                            .revoked(revokedNames.size())
                            .revokedNames(revokedNames)
                            .build();
                })
                .toList();
    }

    // ======================== SV-14. Defect Trend ========================

    @Override
    public List<DefectTrendPoint> getSvDefectTrend(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Defect> allDefects = lineIds.isEmpty() ? List.of()
                : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        // Group by detected_date, last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        Map<LocalDate, List<Defect>> byDate = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null && !d.getDetectedDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.groupingBy(Defect::getDetectedDate, TreeMap::new, Collectors.toList()));

        return byDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Defect> defects = entry.getValue();

                    Map<String, List<Defect>> byDescription = defects.stream()
                            .collect(Collectors.groupingBy(
                                    d -> d.getDefectDescription() + "|" +
                                            (d.getProcess() != null ? d.getProcess().getName() : "N/A")));

                    List<DefectErrorDetail> errors = byDescription.entrySet().stream()
                            .map(e -> {
                                String[] parts = e.getKey().split("\\|", 2);
                                return DefectErrorDetail.builder()
                                        .content(parts[0])
                                        .process(parts.length > 1 ? parts[1] : "N/A")
                                        .quantity(e.getValue().size())
                                        .build();
                            })
                            .toList();

                    return DefectTrendPoint.builder()
                            .date(date.format(formatter))
                            .totalErrors(defects.size())
                            .errors(errors)
                            .build();
                })
                .toList();
    }

    // ======================== SV-15. Defect by Process ========================

    @Override
    public List<StageDistribution> getSvDefectByProcess(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<Defect> allDefects = lineIds.isEmpty() ? List.of()
                : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (Defect d : allDefects) {
            String processName = d.getProcess() != null ? d.getProcess().getName() : "Khác";
            countByProcess.merge(processName, 1, Integer::sum);
        }

        int colorIndex = 0;
        List<StageDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByProcess.entrySet()) {
            result.add(StageDistribution.builder()
                    .stage(entry.getKey())
                    .value(entry.getValue())
                    .color(PIE_COLORS[colorIndex % PIE_COLORS.length])
                    .build());
            colorIndex++;
        }

        return result;
    }

    // ======================== SV-16. Training Samples by Process ========================

    @Override
    public List<StageDistribution> getSvSampleByProcess(Long groupId, Long lineId) {
        List<Long> lineIds = resolveLineIds(groupId, lineId);
        List<TrainingSample> allSamples = lineIds.isEmpty() ? List.of()
                : trainingSampleRepository.findByProductLineIdInAndDeleteFlagFalse(lineIds);

        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (TrainingSample s : allSamples) {
            String processName = s.getProcess() != null ? s.getProcess().getName() : "Khác";
            countByProcess.merge(processName, 1, Integer::sum);
        }

        int colorIndex = 0;
        List<StageDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByProcess.entrySet()) {
            result.add(StageDistribution.builder()
                    .stage(entry.getKey())
                    .value(entry.getValue())
                    .color(PIE_COLORS[colorIndex % PIE_COLORS.length])
                    .build());
            colorIndex++;
        }

        return result;
    }

    // ======================== MNG Dashboard ========================

    /**
     * Resolve danh sách sections mà Manager hiện tại quản lý.
     * Khi sectionId != null → trả về [section đó].
     * Khi sectionId == null → tất cả sections mà Manager được gán.
     */
    private List<Section> resolveMngSections(Long sectionId) {
        if (sectionId != null) {
            return sectionRepository.findById(sectionId)
                    .filter(s -> !s.isDeleteFlag())
                    .map(List::of)
                    .orElse(List.of());
        }
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUser).orElse(null);
        if (user == null)
            return List.of();
        return sectionRepository.findByManagerId(user.getId());
    }

    /**
     * Resolve all group IDs under this manager's sections.
     */
    private List<Long> resolveMngGroupIds(Long sectionId) {
        return resolveMngSections(sectionId).stream()
                .flatMap(s -> s.getGroups().stream())
                .filter(g -> !g.isDeleteFlag())
                .map(Group::getId)
                .distinct()
                .toList();
    }

    /**
     * Resolve all line IDs under this manager's sections.
     * Khi lineId != null → trả về [lineId].
     * Khi lineId == null → tất cả lines trong sections đã resolve.
     */
    private List<Long> resolveMngLineIds(Long sectionId, Long lineId) {
        if (lineId != null) {
            return List.of(lineId);
        }
        List<Long> groupIds = resolveMngGroupIds(sectionId);
        if (groupIds.isEmpty())
            return List.of();
        return groupIds.stream()
                .flatMap(gId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(gId).stream())
                .map(ProductLine::getId)
                .distinct()
                .toList();
    }

    // ======================== MNG-1. Organization Stats ========================

    @Override
    public MngOrgStats getMngOrgStats(Long sectionId, Long lineId) {
        List<Section> sections = resolveMngSections(sectionId);
        if (sections.isEmpty()) {
            return MngOrgStats.builder()
                    .totalGroups(0).totalLines(0).totalTeams(0).totalEmployees(0)
                    .build();
        }

        List<Group> allGroups = sections.stream()
                .flatMap(s -> s.getGroups().stream())
                .filter(g -> !g.isDeleteFlag())
                .toList();

        int totalGroups = allGroups.size();

        int totalTeams = allGroups.stream()
                .flatMap(g -> g.getTeams().stream())
                .filter(t -> !t.isDeleteFlag())
                .mapToInt(t -> 1)
                .sum();

        List<Long> groupIds = allGroups.stream().map(Group::getId).toList();

        List<ProductLine> allLines = groupIds.stream()
                .flatMap(gId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(gId).stream())
                .toList();
        // Filter by lineId if provided
        if (lineId != null) {
            allLines = allLines.stream().filter(l -> l.getId().equals(lineId)).toList();
        }
        int totalLines = allLines.size();

        int totalEmployees = 0;
        for (Long gId : groupIds) {
            totalEmployees += employeeRepository.findAllActiveByGroupId(gId).size();
        }

        return MngOrgStats.builder()
                .totalGroups(totalGroups)
                .totalLines(totalLines)
                .totalTeams(totalTeams)
                .totalEmployees(totalEmployees)
                .build();
    }

    // ======================== MNG-2. Pending Approvals ========================

    @Override
    public List<MngPendingApprovalItem> getMngPendingApprovals(Long sectionId, Long lineId) {
        List<Long> mngLineIds = resolveMngLineIds(sectionId, lineId);

        List<MngPendingApprovalItem> items = new ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 1. Training Plans with WAITING_MANAGER
        List<TrainingPlan> waitingPlans = trainingPlanRepository.findAll().stream()
                .filter(p -> !p.isDeleteFlag())
                .filter(p -> p.getStatus() == ReportStatus.WAITING_MANAGER)
                .filter(p -> mngLineIds.contains(p.getLine().getId()))
                .toList();

        for (TrainingPlan plan : waitingPlans) {
            boolean urgent = plan.getUpdatedAt() != null
                    && java.time.Duration.between(plan.getUpdatedAt(), now).toHours() >= 24;
            items.add(MngPendingApprovalItem.builder()
                    .id(plan.getId())
                    .entityType("TRAINING_PLAN")
                    .title(plan.getTitle() != null ? plan.getTitle() : "Kế hoạch #" + plan.getId())
                    .formCode(plan.getFormCode())
                    .submittedByName(plan.getCreatedBy() != null ? plan.getCreatedBy() : "N/A")
                    .submittedAt(formatTimeAgo(plan.getUpdatedAt()))
                    .isUrgent(urgent)
                    .build());
        }

        // 2. Defect Proposals with WAITING_MANAGER
        List<DefectProposal> waitingDefects = defectProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER).stream()
                .filter(d -> mngLineIds.contains(d.getProductLine().getId()))
                .toList();

        for (DefectProposal dp : waitingDefects) {
            boolean urgent = dp.getUpdatedAt() != null
                    && java.time.Duration.between(dp.getUpdatedAt(), now).toHours() >= 24;
            items.add(MngPendingApprovalItem.builder()
                    .id(dp.getId())
                    .entityType("DEFECT_PROPOSAL")
                    .title("Đề xuất xử lý lỗi #" + dp.getId())
                    .formCode(dp.getFormCode())
                    .submittedByName(dp.getCreatedBy() != null ? dp.getCreatedBy() : "N/A")
                    .submittedAt(formatTimeAgo(dp.getUpdatedAt()))
                    .isUrgent(urgent)
                    .build());
        }

        // 3. Training Sample Proposals with WAITING_MANAGER
        List<TrainingSampleProposal> waitingSamples = trainingSampleProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER).stream()
                .filter(s -> mngLineIds.contains(s.getProductLine().getId()))
                .toList();

        for (TrainingSampleProposal sp : waitingSamples) {
            boolean urgent = sp.getUpdatedAt() != null
                    && java.time.Duration.between(sp.getUpdatedAt(), now).toHours() >= 24;
            items.add(MngPendingApprovalItem.builder()
                    .id(sp.getId())
                    .entityType("TRAINING_SAMPLE_PROPOSAL")
                    .title("Đề xuất mẫu huấn luyện #" + sp.getId())
                    .formCode(sp.getFormCode())
                    .submittedByName(sp.getCreatedBy() != null ? sp.getCreatedBy() : "N/A")
                    .submittedAt(formatTimeAgo(sp.getUpdatedAt()))
                    .isUrgent(urgent)
                    .build());
        }

        return items;
    }

    private String formatTimeAgo(java.time.LocalDateTime dateTime) {
        if (dateTime == null)
            return "N/A";
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long hours = duration.toHours();
        if (hours < 1)
            return duration.toMinutes() + " phút trước";
        if (hours < 24)
            return hours + "h trước";
        long days = duration.toDays();
        return days + " ngày trước";
    }

    // ======================== MNG-3. Training Progress Chart ========================

    @Override
    public List<MngTrainingProgressPoint> getMngTrainingProgress(Long sectionId, Long lineId, Integer month, Integer year) {
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();

        // Current month range
        LocalDate currentStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate currentEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();

        // Previous month range
        YearMonth prevYm = YearMonth.of(targetYear, targetMonth).minusMonths(1);
        LocalDate prevStart = prevYm.atDay(1);
        LocalDate prevEnd = prevYm.atEndOfMonth();

        // Get all lines under this manager's sections
        List<Long> mngLineIds = resolveMngLineIds(sectionId, lineId);
        if (mngLineIds.isEmpty())
            return List.of();

        List<ProductLine> allLines = mngLineIds.stream()
                .map(lId -> productLineRepository.findById(lId).orElse(null))
                .filter(l -> l != null)
                .toList();

        List<MngTrainingProgressPoint> result = new ArrayList<>();

        for (ProductLine line : allLines) {
            // Get results for this line
            List<TrainingResult> lineResults = trainingResultRepository.findByLineIdAndDeleteFlagFalse(line.getId());

            double currentPassRate = calculatePassRate(lineResults, currentStart, currentEnd);
            double previousPassRate = calculatePassRate(lineResults, prevStart, prevEnd);

            result.add(MngTrainingProgressPoint.builder()
                    .lineName(line.getName() != null ? line.getName() : line.getCode())
                    .currentPassRate(currentPassRate)
                    .previousPassRate(previousPassRate)
                    .build());
        }

        return result;
    }

    private double calculatePassRate(List<TrainingResult> results, LocalDate start, LocalDate end) {
        long totalEvaluated = 0;
        long totalPassed = 0;

        for (TrainingResult result : results) {
            List<TrainingResultDetail> details = trainingResultDetailRepository
                    .findByTrainingResultId(result.getId());
            for (TrainingResultDetail d : details) {
                if (d.getActualDate() != null && d.getIsPass() != null
                        && !d.getActualDate().isBefore(start)
                        && !d.getActualDate().isAfter(end)) {
                    totalEvaluated++;
                    if (Boolean.TRUE.equals(d.getIsPass())) {
                        totalPassed++;
                    }
                }
            }
        }

        return totalEvaluated > 0
                ? Math.round((double) totalPassed / totalEvaluated * 1000.0) / 10.0
                : 0;
    }

    // ======================== MNG-4. KPI ========================

    @Override
    public SvKpiData getMngKpi(Long sectionId, Long lineId, Integer year, Integer month) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();
        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate prevMonthEnd = YearMonth.of(prevMonthStart.getYear(), prevMonthStart.getMonthValue()).atEndOfMonth();

        // --- 1. Pass Rate ---
        long currentPassed = 0, currentEvaluated = 0;
        long prevPassed = 0, prevEvaluated = 0;

        List<TrainingResult> allResults = new ArrayList<>();
        for (Long lId : lineIds) {
            allResults.addAll(trainingResultRepository.findByLineIdAndDeleteFlagFalse(lId));
        }

        for (TrainingResult result : allResults) {
            List<TrainingResultDetail> details = trainingResultDetailRepository
                    .findByTrainingResultId(result.getId());
            for (TrainingResultDetail d : details) {
                if (d.getActualDate() != null && d.getIsPass() != null) {
                    if (!d.getActualDate().isBefore(monthStart) && !d.getActualDate().isAfter(monthEnd)) {
                        currentEvaluated++;
                        if (Boolean.TRUE.equals(d.getIsPass())) currentPassed++;
                    }
                    if (!d.getActualDate().isBefore(prevMonthStart) && !d.getActualDate().isAfter(prevMonthEnd)) {
                        prevEvaluated++;
                        if (Boolean.TRUE.equals(d.getIsPass())) prevPassed++;
                    }
                }
            }
        }

        double currentPassPct = currentEvaluated > 0 ? (double) currentPassed / currentEvaluated * 100 : 0;
        double prevPassPct = prevEvaluated > 0 ? (double) prevPassed / prevEvaluated * 100 : 0;
        double passRateDiff = Math.round((currentPassPct - prevPassPct) * 10.0) / 10.0;
        String passRateStr = Math.round(currentPassPct * 10.0) / 10.0 + "%";
        String passRateSub = (passRateDiff >= 0 ? "+" : "") + passRateDiff + "% so với tháng trước";

        // --- 2. Defect Count ---
        List<Defect> allDefects = lineIds.isEmpty() ? List.of()
                : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);
        long currentDefects = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null
                        && !d.getDetectedDate().isBefore(monthStart)
                        && !d.getDetectedDate().isAfter(monthEnd))
                .count();
        long prevDefects = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null
                        && !d.getDetectedDate().isBefore(prevMonthStart)
                        && !d.getDetectedDate().isAfter(prevMonthEnd))
                .count();
        double defectDiff = prevDefects > 0
                ? Math.round((double) (currentDefects - prevDefects) / prevDefects * 1000.0) / 10.0
                : 0;
        String defectSub = (defectDiff >= 0 ? "+" : "") + defectDiff + "% so với tháng trước";

        // --- 3. Training Coverage ---
        List<Long> mngGroupIds = resolveMngGroupIds(sectionId);
        List<Team> teams = teamRepository.findByGroupIdIn(mngGroupIds);
        long totalEmployees = 0;
        long employeesWithValidSkill = 0;

        for (Team team : teams) {
            if (team.getEmployees() != null) {
                for (Employee emp : team.getEmployees()) {
                    if (emp.isDeleteFlag()) continue;
                    totalEmployees++;
                    List<EmployeeSkill> skills = employeeSkillRepository
                            .findByEmployeeIdAndDeleteFlagFalse(emp.getId());
                    boolean hasValid = skills.stream()
                            .anyMatch(s -> s.getStatus() == EmployeeSkillStatus.VALID);
                    if (hasValid) employeesWithValidSkill++;
                }
            }
        }
        double coveragePct = totalEmployees > 0
                ? Math.round((double) employeesWithValidSkill / totalEmployees * 1000.0) / 10.0
                : 0;

        // --- 4. Pending Approval Count (WAITING_MANAGER) ---
        long pendingPlans = trainingPlanRepository.findAll().stream()
                .filter(p -> !p.isDeleteFlag())
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> p.getStatus() == ReportStatus.WAITING_MANAGER)
                .count();
        long pendingResults = allResults.stream()
                .filter(r -> r.getStatus() == ReportStatus.WAITING_MANAGER)
                .count();
        int totalPending = (int) (pendingPlans + pendingResults);

        return SvKpiData.builder()
                .passRate(passRateStr)
                .passRateSub(passRateSub)
                .defectCount((int) currentDefects)
                .defectSub(defectSub)
                .trainingCoverage(coveragePct + "%")
                .coverageSub("Nhân viên có chứng chỉ hợp lệ")
                .pendingApprovalCount(totalPending)
                .pendingSub(totalPending > 0 ? "Hồ sơ đang chờ phê duyệt" : "Không có hồ sơ chờ duyệt")
                .build();
    }

    // ======================== MNG-5. Training Effectiveness ========================

    @Override
    public List<SvTrainingEffectivenessPoint> getMngTrainingEffectiveness(Long sectionId, Long lineId, Integer months) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        int numMonths = (months != null && months > 0) ? months : 6;
        LocalDate today = LocalDate.now();

        List<TrainingResult> allResults = new ArrayList<>();
        for (Long lId : lineIds) {
            allResults.addAll(trainingResultRepository.findByLineIdAndDeleteFlagFalse(lId));
        }

        List<TrainingResultDetail> allDetails = new ArrayList<>();
        for (TrainingResult result : allResults) {
            allDetails.addAll(trainingResultDetailRepository.findByTrainingResultId(result.getId()));
        }

        List<Defect> allDefects = lineIds.isEmpty() ? List.of()
                : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        List<SvTrainingEffectivenessPoint> points = new ArrayList<>();

        for (int i = numMonths - 1; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            LocalDate monthStart = LocalDate.of(monthDate.getYear(), monthDate.getMonthValue(), 1);
            LocalDate monthEnd = YearMonth.of(monthDate.getYear(), monthDate.getMonthValue()).atEndOfMonth();

            int trainingCount = (int) allDetails.stream()
                    .filter(d -> d.getActualDate() != null
                            && !d.getActualDate().isBefore(monthStart)
                            && !d.getActualDate().isAfter(monthEnd))
                    .count();

            int defectCount = (int) allDefects.stream()
                    .filter(d -> d.getDetectedDate() != null
                            && !d.getDetectedDate().isBefore(monthStart)
                            && !d.getDetectedDate().isAfter(monthEnd))
                    .count();

            points.add(SvTrainingEffectivenessPoint.builder()
                    .month(monthNames[monthDate.getMonthValue() - 1])
                    .trainingHours(trainingCount)
                    .defects(defectCount)
                    .build());
        }

        return points;
    }

    // ======================== MNG-6. Training Execution ========================

    @Override
    public List<TrainingExecutionPoint> getMngTrainingExecution(Long sectionId, Long lineId, Integer year, Integer month) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        LocalDate today = LocalDate.now();
        int targetYear = year != null ? year : today.getYear();
        int targetMonth = month != null ? month : today.getMonthValue();

        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = YearMonth.of(targetYear, targetMonth).atEndOfMonth();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        // Get all plans for these lines
        List<TrainingPlan> plans = trainingPlanRepository.findAll().stream()
                .filter(p -> !p.isDeleteFlag())
                .filter(p -> lineIds.contains(p.getLine().getId()))
                .filter(p -> p.getStatus() == ReportStatus.APPROVED
                        || p.getStatus() == ReportStatus.ON_GOING
                        || p.getStatus() == ReportStatus.DONE)
                .toList();

        List<TrainingPlanDetail> allDetails = new ArrayList<>();
        for (TrainingPlan plan : plans) {
            allDetails.addAll(trainingPlanDetailRepository.findByTrainingPlanIdAndDeleteFlagFalse(plan.getId()));
        }

        // Filter to this month
        allDetails = allDetails.stream()
                .filter(d -> d.getPlannedDate() != null
                        && !d.getPlannedDate().isBefore(monthStart)
                        && !d.getPlannedDate().isAfter(monthEnd))
                .toList();

        // Build daily points
        List<TrainingExecutionPoint> points = new ArrayList<>();
        int cumulativePlan = 0, cumulativeActual = 0, cumulativeMissed = 0;

        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            long plannedOnDate = allDetails.stream()
                    .filter(d -> d.getPlannedDate().equals(currentDate))
                    .count();
            cumulativePlan += (int) plannedOnDate;

            long doneOnDate = allDetails.stream()
                    .filter(d -> d.getPlannedDate().equals(currentDate) && d.getStatus() == TrainingPlanDetailStatus.DONE)
                    .count();
            cumulativeActual += (int) doneOnDate;

            if (!date.isAfter(today)) {
                long missedOnDate = allDetails.stream()
                        .filter(d -> d.getPlannedDate().equals(currentDate) && d.getStatus() != TrainingPlanDetailStatus.DONE)
                        .count();
                cumulativeMissed += (int) missedOnDate;
            }

            points.add(TrainingExecutionPoint.builder()
                    .date(date.format(dateFormatter))
                    .keHoach(cumulativePlan)
                    .thucTe(cumulativeActual)
                    .biLo(cumulativeMissed)
                    .build());
        }

        return points;
    }

    // ======================== MNG-7. Skill Certificates ========================

    @Override
    public List<SkillCertificateItem> getMngSkillCertificates(Long sectionId, Long lineId) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        List<Process> processes = lineIds.isEmpty() ? List.of()
                : processRepository.findByProductLineIdInAndDeleteFlagFalse(lineIds);

        return processes.stream()
                .sorted(Comparator.comparing(Process::getCode))
                .map(process -> {
                    List<EmployeeSkill> skills = employeeSkillRepository
                            .findByProcessIdAndDeleteFlagFalse(process.getId());

                    List<String> validNames = new ArrayList<>();
                    List<String> expiringNames = new ArrayList<>();
                    List<String> revokedNames = new ArrayList<>();

                    for (EmployeeSkill skill : skills) {
                        String empName = skill.getEmployee() != null ? skill.getEmployee().getFullName() : "N/A";

                        if (skill.getStatus() == EmployeeSkillStatus.VALID) {
                            validNames.add(empName);
                        } else if (skill.getStatus() == EmployeeSkillStatus.PENDING_REVIEW) {
                            String suffix = skill.getExpiryDate() != null
                                    ? " (" + skill.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM")) + ")"
                                    : "";
                            expiringNames.add(empName + suffix);
                        } else if (skill.getStatus() == EmployeeSkillStatus.REVOKED) {
                            revokedNames.add(empName + " (Đã thu hồi)");
                        }
                    }

                    return SkillCertificateItem.builder()
                            .process(process.getCode() + " (" + process.getName() + ")")
                            .valid(validNames.size())
                            .validNames(validNames)
                            .expiring(expiringNames.size())
                            .expiringNames(expiringNames)
                            .revoked(revokedNames.size())
                            .revokedNames(revokedNames)
                            .build();
                })
                .toList();
    }

    // ======================== MNG-8. Defect Trend ========================

    @Override
    public List<DefectTrendPoint> getMngDefectTrend(Long sectionId, Long lineId) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        List<Defect> allDefects = lineIds.isEmpty() ? List.of()
                : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        Map<LocalDate, List<Defect>> byDate = allDefects.stream()
                .filter(d -> d.getDetectedDate() != null && !d.getDetectedDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.groupingBy(Defect::getDetectedDate, TreeMap::new, Collectors.toList()));

        return byDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Defect> defects = entry.getValue();

                    Map<String, List<Defect>> byDescription = defects.stream()
                            .collect(Collectors.groupingBy(
                                    d -> d.getDefectDescription() + "|" +
                                            (d.getProcess() != null ? d.getProcess().getName() : "N/A")));

                    List<DefectErrorDetail> errors = byDescription.entrySet().stream()
                            .map(e -> {
                                String[] parts = e.getKey().split("\\|", 2);
                                return DefectErrorDetail.builder()
                                        .content(parts[0])
                                        .process(parts.length > 1 ? parts[1] : "N/A")
                                        .quantity(e.getValue().size())
                                        .build();
                            })
                            .toList();

                    return DefectTrendPoint.builder()
                            .date(date.format(formatter))
                            .totalErrors(defects.size())
                            .errors(errors)
                            .build();
                })
                .toList();
    }

    // ======================== MNG-9. Defect by Process ========================

    @Override
    public List<StageDistribution> getMngDefectByProcess(Long sectionId, Long lineId) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        List<Defect> allDefects = lineIds.isEmpty() ? List.of()
                : defectRepository.findAllByProductLineIdsAndDeleteFlagFalse(lineIds);

        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (Defect d : allDefects) {
            String processName = d.getProcess() != null ? d.getProcess().getName() : "Khác";
            countByProcess.merge(processName, 1, Integer::sum);
        }

        int colorIndex = 0;
        List<StageDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByProcess.entrySet()) {
            result.add(StageDistribution.builder()
                    .stage(entry.getKey())
                    .value(entry.getValue())
                    .color(PIE_COLORS[colorIndex % PIE_COLORS.length])
                    .build());
            colorIndex++;
        }

        return result;
    }

    // ======================== MNG-10. Training Samples by Process ========================

    @Override
    public List<StageDistribution> getMngSampleByProcess(Long sectionId, Long lineId) {
        List<Long> lineIds = resolveMngLineIds(sectionId, lineId);
        List<TrainingSample> allSamples = lineIds.isEmpty() ? List.of()
                : trainingSampleRepository.findByProductLineIdInAndDeleteFlagFalse(lineIds);

        Map<String, Integer> countByProcess = new LinkedHashMap<>();
        for (TrainingSample s : allSamples) {
            String processName = s.getProcess() != null ? s.getProcess().getName() : "Khác";
            countByProcess.merge(processName, 1, Integer::sum);
        }

        int colorIndex = 0;
        List<StageDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByProcess.entrySet()) {
            result.add(StageDistribution.builder()
                    .stage(entry.getKey())
                    .value(entry.getValue())
                    .color(PIE_COLORS[colorIndex % PIE_COLORS.length])
                    .build());
            colorIndex++;
        }

        return result;
    }
}
