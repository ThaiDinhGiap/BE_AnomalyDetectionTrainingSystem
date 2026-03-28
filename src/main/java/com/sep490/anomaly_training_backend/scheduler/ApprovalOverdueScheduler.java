package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
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
    private final ApprovalFlowStepRepository flowStepRepository;
    private final ApprovalRouteService approvalRouteService;

    @Value("${app.scheduler.approval-overdue-hours:24}")
    private int overdueHours;

    @Scheduled(cron = "${app.scheduler.approval-overdue: 0 0 8,14 * * ?}")
    @Transactional(readOnly = true)
    public void checkAndSendOverdueApprovalReminders() {
        log.info("=== Starting Approval Overdue Check Job ===");
        LocalDateTime threshold = LocalDateTime.now().minusHours(overdueHours);

        try {
            // Lấy tất cả approval flow steps active, xử lý generic
            List<ApprovalFlowStep> activeSteps = flowStepRepository.findAll().stream()
                    .filter(ApprovalFlowStep::getIsActive)
                    .toList();

            // Lấy danh sách các pending status cần kiểm tra
            List<ReportStatus> pendingStatuses = activeSteps.stream()
                    .map(ApprovalFlowStep::getPendingStatus)
                    .distinct()
                    .toList();

            for (ReportStatus pendingStatus : pendingStatuses) {
                checkOverdueApprovals(threshold, pendingStatus, activeSteps);
            }

            log.info("=== Completed Approval Overdue Check Job ===");
        } catch (Exception e) {
            log.error("Error in Approval Overdue Check Job", e);
        }
    }

    private void checkOverdueApprovals(LocalDateTime threshold, ReportStatus pendingStatus, List<ApprovalFlowStep> activeSteps) {
        Map<Long, PendingApprovalSummary> pendingByApprover = new HashMap<>();

        // Tìm step tương ứng để resolve approver
        String requiredPermission = activeSteps.stream()
                .filter(s -> s.getPendingStatus() == pendingStatus)
                .findFirst()
                .map(ApprovalFlowStep::getRequiredPermission)
                .orElse(null);

        if (requiredPermission == null) return;

        trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(pendingStatus, threshold)
                .forEach(plan -> {
                    try {
                        Long approverId = approvalRouteService.getApproverIdForStep(
                                plan.getTeam().getGroup().getId(), requiredPermission);
                        pendingByApprover
                                .computeIfAbsent(approverId, k -> new PendingApprovalSummary())
                                .addTrainingPlan(plan);
                    } catch (Exception e) {
                        log.warn("Could not resolve approver for plan {}: {}", plan.getId(), e.getMessage());
                    }
                });

        defectProposalRepository
                .findByStatusAndDeleteFlagFalse(pendingStatus).stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .forEach(r -> {
                    try {
                        Long approverId = approvalRouteService.getApproverIdForStep(
                                r.getProductLine().getGroup().getId(), requiredPermission);
                        pendingByApprover
                                .computeIfAbsent(approverId, k -> new PendingApprovalSummary())
                                .addDefectProposal(r);
                    } catch (Exception e) {
                        log.warn("Could not resolve approver for defect {}: {}", r.getId(), e.getMessage());
                    }
                });

        trainingSampleProposalRepository
                .findByStatusAndDeleteFlagFalse(pendingStatus).stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .forEach(r -> {
                    try {
                        Long approverId = approvalRouteService.getApproverIdForStep(
                                r.getProductLine().getGroup().getId(), requiredPermission);
                        pendingByApprover
                                .computeIfAbsent(approverId, k -> new PendingApprovalSummary())
                                .addTrainingSampleProposal(r);
                    } catch (Exception e) {
                        log.warn("Could not resolve approver for sample {}: {}", r.getId(), e.getMessage());
                    }
                });

        trainingResultRepository
                .findByStatusAndDeleteFlagFalse(pendingStatus).stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .forEach(r -> {
                    try {
                        Long approverId = approvalRouteService.getApproverIdForStep(
                                r.getTeam().getGroup().getId(), requiredPermission);
                        pendingByApprover
                                .computeIfAbsent(approverId, k -> new PendingApprovalSummary())
                                .addTrainingResult(r);
                    } catch (Exception e) {
                        log.warn("Could not resolve approver for result {}: {}", r.getId(), e.getMessage());
                    }
                });

        pendingByApprover.forEach((approverId, summary) ->
                notify(approverId, summary, NotificationType.APPROVAL_OVERDUE_SV, "Approver"));
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