package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.DefectReport;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingTopicReport;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectReportRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingTopicReportRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class ApprovalOverdueScheduler {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingResultRepository trainingResultRepository;
    private final DefectReportRepository defectReportRepository;
    private final TrainingTopicReportRepository trainingTopicReportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.scheduler.approval-overdue-hours:24}")
    private int overdueHours;

    /**
     * Use Case 5: Nhắc nhở phê duyệt tồn đọng
     * Chạy lúc 8:00 và 14:00 hàng ngày
     */
    @Scheduled(cron = "${app.scheduler.approval-overdue: 0 0 8,14 * * ?}")
    @Transactional(readOnly = true)
    public void checkAndSendOverdueApprovalReminders() {
        log.info("=== Starting Approval Overdue Check Job ===");

        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(overdueHours);

        try {
            // 1. Check Supervisor overdue approvals
            checkSupervisorOverdueApprovals(overdueThreshold);

            // 2. Check Manager overdue approvals
            checkManagerOverdueApprovals(overdueThreshold);

            log.info("=== Completed Approval Overdue Check Job ===");

        } catch (Exception e) {
            log.error("Error in Approval Overdue Check Job", e);
        }
    }

    private void checkSupervisorOverdueApprovals(LocalDateTime threshold) {
        log.info("Checking Supervisor overdue approvals...");

        Map<Long, PendingApprovalSummary> supervisorPendings = new HashMap<>();

        // Training Plans waiting for SV
        List<TrainingPlan> pendingPlans = trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_SV.toString(), threshold);

        for (TrainingPlan plan : pendingPlans) {
            Long svId = plan.getGroup().getSupervisor().getId();
            supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                    .addTrainingPlan(plan);
        }

        // Defect Reports waiting for SV
        List<DefectReport> pendingDefectReports = defectReportRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV);

        for (DefectReport report : pendingDefectReports) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long svId = report.getGroup().getSupervisor().getId();
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addDefectReport(report);
            }
        }

        // Training Topic Reports waiting for SV
        List<TrainingTopicReport> pendingTopicReports = trainingTopicReportRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV);

        for (TrainingTopicReport report : pendingTopicReports) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long svId = report.getGroup().getSupervisor().getId();
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addTrainingTopicReport(report);
            }
        }

        // Send notifications to supervisors
        for (Map.Entry<Long, PendingApprovalSummary> entry : supervisorPendings.entrySet()) {
            sendOverdueNotificationToUser(entry.getKey(), entry.getValue(), "Supervisor");
        }
    }

    private void checkManagerOverdueApprovals(LocalDateTime threshold) {
        log.info("Checking Manager overdue approvals...");

        Map<Long, PendingApprovalSummary> managerPendings = new HashMap<>();

        // Training Plans waiting for Manager
        List<TrainingPlan> pendingPlans = trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_MANAGER.toString(), threshold);

        for (TrainingPlan plan : pendingPlans) {
            Long managerId = plan.getGroup().getSection().getManager().getId();
            managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                    .addTrainingPlan(plan);
        }

        // Training Results waiting for Manager
        List<TrainingResult> pendingResults = trainingResultRepository
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_MANAGER.toString(), threshold);

        for (TrainingResult result : pendingResults) {
            Long managerId = result.getGroup().getSection().getManager().getId();
            managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                    .addTrainingResult(result);
        }

        // Defect Reports waiting for Manager
        List<DefectReport> pendingDefectReports = defectReportRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER);

        for (DefectReport report : pendingDefectReports) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long managerId = report.getGroup().getSection().getManager().getId();
                managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                        .addDefectReport(report);
            }
        }

        // Training Topic Reports waiting for Manager
        List<TrainingTopicReport> pendingTopicReports = trainingTopicReportRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER);

        for (TrainingTopicReport report : pendingTopicReports) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long managerId = report.getGroup().getSection().getManager().getId();
                managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                        .addTrainingTopicReport(report);
            }
        }

        // Send notifications to managers
        for (Map.Entry<Long, PendingApprovalSummary> entry : managerPendings.entrySet()) {
            sendOverdueNotificationToUser(entry.getKey(), entry.getValue(), "Manager");
        }
    }

    private void sendOverdueNotificationToUser(Long userId, PendingApprovalSummary summary, String role) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getEmail() == null) {
            log.warn("Cannot send notification to user {}: user not found or no email", userId);
            return;
        }

        if (summary.getTotalCount() == 0) {
            return;
        }

        String subject = String.format("[Nhắc nhở] Bạn có %d phê duyệt đang chờ xử lý", summary.getTotalCount());
        String body = buildNotificationBody(summary, role);

        NotificationType notificationType = "Supervisor".equals(role)
                ? NotificationType.APPROVAL_OVERDUE_SV
                : NotificationType.APPROVAL_OVERDUE_MANAGER;

        NotificationRequest request = NotificationRequest.builder()
                .recipientUserId(userId)
                .recipientEmail(user.getEmail())
                .recipientName(user.getFullName())
                .type(notificationType)
                .channel(NotificationChannel.EMAIL)
                .build();

        notificationService.sendNotification(request);
        log.info("Sent overdue approval reminder to {} ({}): {} items", user.getFullName(), role, summary.getTotalCount());
    }

    private String buildNotificationBody(PendingApprovalSummary summary, String role) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(role).append(",\n\n");
        sb.append("Bạn có các phê duyệt đang chờ xử lý:\n\n");

        if (!summary.getTrainingPlans().isEmpty()) {
            sb.append("- Kế hoạch huấn luyện: ").append(summary.getTrainingPlans().size()).append(" kế hoạch\n");
        }
        if (!summary.getTrainingResults().isEmpty()) {
            sb.append("- Kết quả huấn luyện: ").append(summary.getTrainingResults().size()).append(" kết quả\n");
        }
        if (!summary.getDefectReports().isEmpty()) {
            sb.append("- Báo cáo lỗi: ").append(summary.getDefectReports().size()).append(" báo cáo\n");
        }
        if (!summary.getTrainingTopicReports().isEmpty()) {
            sb.append("- Mẫu huấn luyện: ").append(summary.getTrainingTopicReports().size()).append(" báo cáo\n");
        }

        sb.append("\nVui lòng đăng nhập hệ thống để xử lý.\n");
        sb.append("\nTrân trọng,\nHệ thống Anomaly Training");

        return sb.toString();
    }

    /**
     * Inner class to hold pending approval summary
     */
    private static class PendingApprovalSummary {
        private final List<TrainingPlan> trainingPlans = new ArrayList<>();
        private final List<TrainingResult> trainingResults = new ArrayList<>();
        private final List<DefectReport> defectReports = new ArrayList<>();
        private final List<TrainingTopicReport> trainingTopicReports = new ArrayList<>();

        public void addTrainingPlan(TrainingPlan plan) {
            trainingPlans.add(plan);
        }

        public void addTrainingResult(TrainingResult result) {
            trainingResults.add(result);
        }

        public void addDefectReport(DefectReport report) {
            defectReports.add(report);
        }

        public void addTrainingTopicReport(TrainingTopicReport report) {
            trainingTopicReports.add(report);
        }

        public List<TrainingPlan> getTrainingPlans() {
            return trainingPlans;
        }

        public List<TrainingResult> getTrainingResults() {
            return trainingResults;
        }

        public List<DefectReport> getDefectReports() {
            return defectReports;
        }

        public List<TrainingTopicReport> getTrainingTopicReports() {
            return trainingTopicReports;
        }

        public int getTotalCount() {
            return trainingPlans.size() + trainingResults.size() + defectReports.size() + trainingTopicReports.size();
        }
    }
}
