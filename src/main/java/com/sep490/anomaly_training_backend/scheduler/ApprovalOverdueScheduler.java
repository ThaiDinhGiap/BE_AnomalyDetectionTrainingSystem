package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.notification.NotificationService;
import lombok.Data;
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
    private final DefectProposalRepository defectProposalRepository;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
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
            checkSupervisorOverdueApprovals(overdueThreshold);

            checkManagerOverdueApprovals(overdueThreshold);

            log.info("=== Completed Approval Overdue Check Job ===");

        } catch (Exception e) {
            log.error("Error in Approval Overdue Check Job", e);
        }
    }

    private void checkSupervisorOverdueApprovals(LocalDateTime threshold) {
        log.info("Checking Supervisor overdue approvals...");

        Map<Long, PendingApprovalSummary> supervisorPendings = new HashMap<>();

        List<TrainingPlan> pendingPlans = trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_SV, threshold);

        for (TrainingPlan plan : pendingPlans) {
            Long svId = plan.getTeam().getGroup().getSupervisor().getId();
            supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                    .addTrainingPlan(plan);
        }

        // Defect Proposals waiting for SV
        List<DefectProposal> pendingDefectProposals = defectProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV);

        for (DefectProposal report : pendingDefectProposals) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long svId = report.getProductLine().getGroup().getSupervisor().getId();
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addDefectProposal(report);
            }
        }

        // Training Topic Reports waiting for SV
        List<TrainingSampleProposal> pendingTrainingSampleProposals = trainingSampleProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV);

        for (TrainingSampleProposal report : pendingTrainingSampleProposals) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long svId = report.getProductLine().getGroup().getSupervisor().getId();
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addTrainingSampleProposal(report);
            }
        }

        // List Training Results waiting for SV
        List<TrainingResult> pendingTrainingResults = trainingResultRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV);

        for (TrainingResult report : pendingTrainingResults) {
            if (report.getUpdatedAt() != null && report.getUpdatedAt().isBefore(threshold)) {
                Long svId = report.getTeam().getGroup().getSupervisor().getId();
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addTrainingResult(report);
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
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_MANAGER, threshold);

        for (TrainingPlan plan : pendingPlans) {
            Long managerId = plan.getTeam().getGroup().getSection().getManager().getId();
            managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                    .addTrainingPlan(plan);
        }

        // Defect Proposals waiting for Manager
        List<DefectProposal> pendingDefectProposals = defectProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER);

        for (DefectProposal proposal : pendingDefectProposals) {
            if (proposal.getUpdatedAt() != null && proposal.getUpdatedAt().isBefore(threshold)) {
                Long managerId = proposal.getProductLine().getGroup().getSection().getManager().getId();
                managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                        .addDefectProposal(proposal);
            }
        }

        // Training Sample Proposals waiting for Manager
        List<TrainingSampleProposal> pendingTrainingSampleProposals = trainingSampleProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER);

        for (TrainingSampleProposal proposal : pendingTrainingSampleProposals) {
            if (proposal.getUpdatedAt() != null && proposal.getUpdatedAt().isBefore(threshold)) {
                Long managerId = proposal.getProductLine().getGroup().getSection().getManager().getId();
                managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                        .addTrainingSampleProposal(proposal);
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
        if (!summary.getDefectProposals().isEmpty()) {
            sb.append("- Báo cáo lỗi: ").append(summary.getDefectProposals().size()).append(" báo cáo\n");
        }
        if (!summary.getTrainingSampleProposals().isEmpty()) {
            sb.append("- Mẫu huấn luyện: ").append(summary.getTrainingSampleProposals().size()).append(" báo cáo\n");
        }

        sb.append("\nVui lòng đăng nhập hệ thống để xử lý.\n");
        sb.append("\nTrân trọng,\nHệ thống Anomaly Training");

        return sb.toString();
    }

    @Data
    private static class PendingApprovalSummary {
        private final List<TrainingPlan> trainingPlans = new ArrayList<>();
        private final List<TrainingResult> trainingResults = new ArrayList<>();
        private final List<DefectProposal> defectProposals = new ArrayList<>();
        private final List<TrainingSampleProposal> trainingSampleProposals = new ArrayList<>();

        public void addTrainingPlan(TrainingPlan plan) {
            trainingPlans.add(plan);
        }

        public void addTrainingResult(TrainingResult result) {
            trainingResults.add(result);
        }

        public void addDefectProposal(DefectProposal proposal) {
            defectProposals.add(proposal);
        }

        public void addTrainingSampleProposal(TrainingSampleProposal proposal) {
            trainingSampleProposals.add(proposal);
        }

        public int getTotalCount() {
            return trainingPlans.size() + trainingResults.size() + defectProposals.size() + trainingSampleProposals.size();
        }
    }
}
