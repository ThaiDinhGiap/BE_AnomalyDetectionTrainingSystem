package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import com.sep490.anomaly_training_backend.service.notification.NotificationService;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class ApprovalOverdueScheduler {

    private final List<ApprovalHandler> allHandlers;
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
            List<ApprovalFlowStep> activeSteps = flowStepRepository.findAll().stream()
                    .filter(ApprovalFlowStep::getIsActive)
                    .toList();

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
        // Resolve required permission cho pending status này
        String requiredPermission = activeSteps.stream()
                .filter(s -> s.getPendingStatus() == pendingStatus)
                .findFirst()
                .map(ApprovalFlowStep::getRequiredPermission)
                .orElse(null);

        if (requiredPermission == null) return;

        // Map: approverId → { displayLabel → count }
        Map<Long, Map<String, Integer>> pendingByApprover = new HashMap<>();

        // Generic loop — iterate over all handlers
        for (ApprovalHandler handler : allHandlers) {
            List<OverdueItem> overdueItems = handler.findOverdueItems(pendingStatus, threshold);
            if (overdueItems.isEmpty()) continue;

            String label = handler.getDisplayLabel();

            for (OverdueItem item : overdueItems) {
                try {
                    Long approverId = approvalRouteService.getApproverIdForStep(
                            item.groupId(), requiredPermission);
                    pendingByApprover
                            .computeIfAbsent(approverId, k -> new HashMap<>())
                            .merge(label, 1, Integer::sum);
                } catch (Exception e) {
                    log.warn("Could not resolve approver for {} id={}: {}",
                            handler.getType(), item.entityId(), e.getMessage());
                }
            }
        }

        // Notify each approver
        pendingByApprover.forEach((approverId, labelCounts) ->
                notify(approverId, labelCounts));
    }

    private void notify(Long userId, Map<String, Integer> labelCounts) {
        int totalCount = labelCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalCount == 0) return;

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
                            .type(NotificationType.APPROVAL_OVERDUE_SV)
                            .channel(NotificationChannel.EMAIL)
                            .build()
            );
        }

        // ── In-App ──
        String message = buildInAppMessage(labelCounts, totalCount);
        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(userId)
                        .title("Phê duyệt đang chờ xử lý")
                        .message(message)
                        .type(InAppNotificationType.ACTION_REQUIRED)
                        .actionUrl("/approvals?filter=overdue")
                        .build()
        );

        log.info("[ApprovalOverdue] Notified {} ({}) — {} items",
                user.getFullName(), userId, totalCount);
    }

    private String buildInAppMessage(Map<String, Integer> labelCounts, int totalCount) {
        List<String> parts = labelCounts.entrySet().stream()
                .map(e -> e.getValue() + " " + e.getKey())
                .collect(Collectors.toCollection(ArrayList::new));

        return String.format("Bạn có %d mục chờ phê duyệt quá %d giờ: %s.",
                totalCount, overdueHours, String.join(", ", parts));
    }
}