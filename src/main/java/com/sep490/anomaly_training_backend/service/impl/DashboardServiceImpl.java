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

        // Get all approved training plans for this line
        List<TrainingPlan> plans = trainingPlanRepository.findByLineIdAndDeleteFlagFalse(lineId);
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

        // Failed & missed from result details
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

        // Monthly defects
        List<Defect> allDefects = defectRepository.findAllByProductLineAndDeleteFlagFalse(lineId);
        int monthlyDefects = (int) allDefects.stream()
                .filter(d -> d.getDetectedDate() != null &&
                        !d.getDetectedDate().isBefore(monthStart) &&
                        !d.getDetectedDate().isAfter(monthEnd))
                .count();

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

    // ======================== 2. Rejected Reports ========================

    @Override
    public List<RejectedReportItem> getRejectedReports(Long lineId, String type) {
        List<RejectedReportItem> items = new ArrayList<>();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        List<ReportStatus> rejectedStatuses = List.of(
                ReportStatus.REJECTED_BY_SV,
                ReportStatus.REJECTED_BY_MANAGER);

        // Rejected Training Plans
        if (type == null || "Tất cả".equalsIgnoreCase(type) || "Kế hoạch huấn luyện".equalsIgnoreCase(type)) {
            List<TrainingPlan> plans = trainingPlanRepository.findByLineIdAndDeleteFlagFalse(lineId);
            plans.stream()
                    .filter(p -> rejectedStatuses.contains(p.getStatus()))
                    .filter(p -> currentUser.equals(p.getCreatedBy()))
                    .forEach(p -> items.add(RejectedReportItem.builder()
                            .id(p.getId())
                            .type("Kế hoạch huấn luyện")
                            .title(p.getTitle() != null ? p.getTitle() : "Kế hoạch huấn luyện #" + p.getId())
                            .description(p.getNote() != null ? p.getNote() : "")
                            .entityType("TRAINING_PLAN")
                            .build()));
        }

        // Rejected Training Results
        if (type == null || "Tất cả".equalsIgnoreCase(type) || "Kết quả huấn luyện".equalsIgnoreCase(type)) {
            List<TrainingResult> results = trainingResultRepository.findByLineIdAndDeleteFlagFalse(lineId);
            results.stream()
                    .filter(r -> rejectedStatuses.contains(r.getStatus()))
                    .filter(r -> currentUser.equals(r.getCreatedBy()))
                    .forEach(r -> items.add(RejectedReportItem.builder()
                            .id(r.getId())
                            .type("Kết quả huấn luyện")
                            .title(r.getTitle() != null ? r.getTitle() : "Báo cáo kết quả #" + r.getId())
                            .description(r.getNote() != null ? r.getNote() : "")
                            .entityType("TRAINING_RESULT")
                            .build()));
        }

        // Rejected Defect Proposals
        if (type == null || "Tất cả".equalsIgnoreCase(type) || "Lỗi quá khứ".equalsIgnoreCase(type)) {
            List<DefectProposal> defectProposals = defectProposalRepository.findByProductLineId(lineId);
            defectProposals.stream()
                    .filter(p -> !p.isDeleteFlag() && rejectedStatuses.contains(p.getStatus()))
                    .filter(p -> currentUser.equals(p.getCreatedBy()))
                    .forEach(p -> items.add(RejectedReportItem.builder()
                            .id(p.getId())
                            .type("Lỗi quá khứ")
                            .title("Đề xuất lỗi #" + p.getId())
                            .description(p.getFormCode() != null ? p.getFormCode() : "")
                            .entityType("DEFECT_PROPOSAL")
                            .build()));
        }

        // Rejected Training Sample Proposals
        if (type == null || "Tất cả".equalsIgnoreCase(type) || "Mẫu huấn luyện".equalsIgnoreCase(type)) {
            List<TrainingSampleProposal> sampleProposals = trainingSampleProposalRepository.findByProductLineId(lineId);
            sampleProposals.stream()
                    .filter(p -> !p.isDeleteFlag() && rejectedStatuses.contains(p.getStatus()))
                    .filter(p -> currentUser.equals(p.getCreatedBy()))
                    .forEach(p -> items.add(RejectedReportItem.builder()
                            .id(p.getId())
                            .type("Mẫu huấn luyện")
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
        LocalDate yesterday = today.minusDays(1);
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        List<TrainingPlan> plans = trainingPlanRepository.findByLineIdAndDeleteFlagFalse(lineId);
        List<Long> activePlanIds = plans.stream()
                .filter(p -> (p.getStatus() == ReportStatus.APPROVED || p.getStatus() == ReportStatus.ON_GOING))
                .filter(p -> currentUser.equals(p.getCreatedBy()))
                .map(TrainingPlan::getId)
                .toList();

        // Today's training tasks
        List<TrainingTaskToday> todayList = new ArrayList<>();
        // Failed yesterday
        List<TrainingTaskFailed> failedList = new ArrayList<>();
        // Missed
        List<TrainingTaskMissed> missedList = new ArrayList<>();

        for (Long planId : activePlanIds) {
            List<TrainingPlanDetail> details = trainingPlanDetailRepository
                    .findByTrainingPlanIdAndDeleteFlagFalse(planId);

            for (TrainingPlanDetail d : details) {
                Employee emp = d.getEmployee();
                String empName = emp != null ? emp.getFullName() : "N/A";
                String empCode = emp != null ? emp.getEmployeeCode() : "N/A";

                // Today's tasks
                if (today.equals(d.getPlannedDate()) && d.getStatus() == TrainingPlanDetailStatus.PENDING) {
                    todayList.add(TrainingTaskToday.builder()
                            .id(d.getId())
                            .employeeName(empName)
                            .employeeCode(empCode)
                            .processName("")
                            .type("Định kỳ")
                            .timeSlot("")
                            .build());
                }

                // Missed (past & still PENDING)
                if (d.getPlannedDate() != null && d.getPlannedDate().isBefore(today)
                        && d.getStatus() == TrainingPlanDetailStatus.PENDING) {
                    missedList.add(TrainingTaskMissed.builder()
                            .id(d.getId())
                            .employeeName(empName)
                            .employeeCode(empCode)
                            .processName("")
                            .date(d.getPlannedDate().format(DateTimeFormatter.ofPattern("dd/MM")))
                            .reason("Chưa thực hiện")
                            .action("Xếp lịch lại")
                            .build());
                }
            }
        }

        // Failed yesterday — from result details
        List<TrainingResultDetail> allFailed = trainingResultDetailRepository.findFailedTrainings(lineId);
        for (TrainingResultDetail rd : allFailed) {
            if (rd.getActualDate() != null && rd.getActualDate().equals(yesterday)) {
                Employee emp = rd.getEmployee();
                failedList.add(TrainingTaskFailed.builder()
                        .id(rd.getId())
                        .employeeName(emp != null ? emp.getFullName() : "N/A")
                        .employeeCode(emp != null ? emp.getEmployeeCode() : "N/A")
                        .processName(rd.getProcess() != null
                                ? rd.getProcess().getCode() + " (" + rd.getProcess().getName() + ")"
                                : "")
                        .date(yesterday.format(DateTimeFormatter.ofPattern("dd/MM")))
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

        List<TrainingPlan> plans = trainingPlanRepository.findByLineIdAndDeleteFlagFalse(lineId);
        List<Long> planIds = plans.stream()
                .filter(p -> p.getStatus() != ReportStatus.DRAFT)
                .filter(p -> currentUser.equals(p.getCreatedBy()))
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
        List<TrainingPlan> plans = trainingPlanRepository.findByLineIdAndDeleteFlagFalse(lineId)
                .stream()
                .filter(p -> currentUser.equals(p.getCreatedBy()))
                .toList();
        List<TrainingPlanDetail> allDetails = new ArrayList<>();

        for (TrainingPlan plan : plans) {
            if (plan.getStatus() == ReportStatus.DRAFT)
                continue;
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
        List<Process> processes = processRepository.findByProductLineIdAndDeleteFlagFalse(lineId);
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
        List<Process> processes = processRepository.findByProductLineIdAndDeleteFlagFalse(lineId);

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
        List<Defect> allDefects = defectRepository.findAllByProductLineAndDeleteFlagFalse(lineId);

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
        List<Defect> allDefects = defectRepository.findAllByProductLineAndDeleteFlagFalse(lineId);

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
        List<TrainingSample> allSamples = trainingSampleRepository.findByProductLineIdAndDeleteFlagFalse(lineId);

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
