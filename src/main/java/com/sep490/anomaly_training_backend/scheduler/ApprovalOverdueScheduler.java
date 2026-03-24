package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
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
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
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
    private final InAppNotificationService inAppService;

    @Value("${app.scheduler.approval-overdue-hours:24}")
    private int overdueHours;

    @Scheduled(cron = "${app.scheduler.approval-overdue: 0 0 8,14 * * ?}")
    @Transactional(readOnly = true)
    public void checkAndSendOverdueApprovalReminders() {
        log.info("=== Starting Approval Overdue Check Job ===");
        LocalDateTime threshold = LocalDateTime.now().minusHours(overdueHours);

        try {
            checkSupervisorOverdueApprovals(threshold);
            checkManagerOverdueApprovals(threshold);
            log.info("=== Completed Approval Overdue Check Job ===");
        } catch (Exception e) {
            log.error("Error in Approval Overdue Check Job", e);
        }
    }

    private void checkSupervisorOverdueApprovals(LocalDateTime threshold) {
        Map<Long, PendingApprovalSummary> pendingBySv = new HashMap<>();

        trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_SV, threshold)
                .forEach(plan -> pendingBySv
                        .computeIfAbsent(plan.getTeam().getGroup().getSupervisor().getId(),
                                k -> new PendingApprovalSummary())
                        .addTrainingPlan(plan));

        defectProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV).stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .forEach(r -> pendingBySv
                        .computeIfAbsent(r.getProductLine().getGroup().getSupervisor().getId(),
                                k -> new PendingApprovalSummary())
                        .addDefectProposal(r));

        trainingSampleProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV).stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .forEach(r -> pendingBySv
                        .computeIfAbsent(r.getProductLine().getGroup().getSupervisor().getId(),
                                k -> new PendingApprovalSummary())
                        .addTrainingSampleProposal(r));

        trainingResultRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_SV).stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .forEach(r -> pendingBySv
                        .computeIfAbsent(r.getTeam().getGroup().getSupervisor().getId(),
                                k -> new PendingApprovalSummary())
                        .addTrainingResult(r));

        pendingBySv.forEach((svId, summary) ->
                notify(svId, summary, NotificationType.APPROVAL_OVERDUE_SV, "Supervisor"));
    }

    private void checkManagerOverdueApprovals(LocalDateTime threshold) {
        Map<Long, PendingApprovalSummary> pendingByManager = new HashMap<>();

        trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(ReportStatus.WAITING_MANAGER, threshold)
                .forEach(plan -> pendingByManager
                        .computeIfAbsent(plan.getTeam().getGroup().getSection().getManager().getId(),
                                k -> new PendingApprovalSummary())
                        .addTrainingPlan(plan));

        defectProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER).stream()
                .filter(p -> p.getUpdatedAt() != null && p.getUpdatedAt().isBefore(threshold))
                .forEach(p -> pendingByManager
                        .computeIfAbsent(p.getProductLine().getGroup().getSection().getManager().getId(),
                                k -> new PendingApprovalSummary())
                        .addDefectProposal(p));

        trainingSampleProposalRepository
                .findByStatusAndDeleteFlagFalse(ReportStatus.WAITING_MANAGER).stream()
                .filter(p -> p.getUpdatedAt() != null && p.getUpdatedAt().isBefore(threshold))
                .forEach(p -> pendingByManager
                        .computeIfAbsent(p.getProductLine().getGroup().getSection().getManager().getId(),
                                k -> new PendingApprovalSummary())
                        .addTrainingSampleProposal(p));

        pendingByManager.forEach((managerId, summary) ->
                notify(managerId, summary, NotificationType.APPROVAL_OVERDUE_MANAGER, "Manager"));
    }

    private void notify(Long userId, PendingApprovalSummary summary,
                        NotificationType emailType, String role) {
        if (summary.getTotalCount() == 0) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("[ApprovalOverdue] User {} not found", userId);
            return;
        }

        // ── Email (async, qua RabbitMQ) ──
        if (user.getEmail() != null) {
            notificationService.sendNotification(
                    NotificationRequest.builder()
                            .recipientUserId(userId)
                            .recipientEmail(user.getEmail())
                            .recipientName(user.getFullName())
                            .type(emailType)
                            .channel(NotificationChannel.EMAIL)
                            .build()
            );
        }

        // ── In-App ──
        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(userId)
                        .title("Phê duyệt đang chờ xử lý")
                        .message(buildInAppMessage(summary, role))
                        .type(InAppNotificationType.ACTION_REQUIRED)
                        .actionUrl("/approvals?filter=overdue")
                        .build()
        );

        log.info("[ApprovalOverdue] Notified {} {} ({}) — {} items",
                role, user.getFullName(), userId, summary.getTotalCount());
    }

    private String buildInAppMessage(PendingApprovalSummary summary, String role) {
        List<String> parts = new ArrayList<>();

        if (!summary.getTrainingPlans().isEmpty())
            parts.add(summary.getTrainingPlans().size() + " kế hoạch huấn luyện");
        if (!summary.getTrainingResults().isEmpty())
            parts.add(summary.getTrainingResults().size() + " kết quả huấn luyện");
        if (!summary.getDefectProposals().isEmpty())
            parts.add(summary.getDefectProposals().size() + " đề xuất lỗi");
        if (!summary.getTrainingSampleProposals().isEmpty())
            parts.add(summary.getTrainingSampleProposals().size() + " đề xuất mẫu");

        return String.format("Bạn có %d mục chờ phê duyệt quá %d giờ: %s.",
                summary.getTotalCount(), overdueHours, String.join(", ", parts));
    }
    
    @Data
    private static class PendingApprovalSummary {
        private final List<TrainingPlan> trainingPlans = new ArrayList<>();
        private final List<TrainingResult> trainingResults = new ArrayList<>();
        private final List<DefectProposal> defectProposals = new ArrayList<>();
        private final List<TrainingSampleProposal> trainingSampleProposals = new ArrayList<>();

        void addTrainingPlan(TrainingPlan p) {
            trainingPlans.add(p);
        }

        void addTrainingResult(TrainingResult r) {
            trainingResults.add(r);
        }

        void addDefectProposal(DefectProposal p) {
            defectProposals.add(p);
        }

        void addTrainingSampleProposal(TrainingSampleProposal p) {
            trainingSampleProposals.add(p);
        }

        int getTotalCount() {
            return trainingPlans.size() + trainingResults.size()
                    + defectProposals.size() + trainingSampleProposals.size();
        }
    }
}