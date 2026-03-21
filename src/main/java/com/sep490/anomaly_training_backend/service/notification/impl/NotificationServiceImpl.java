package com.sep490.anomaly_training_backend.service.notification.impl;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.messaging.producer.NotificationProducer;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.NotificationSetting;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.NotificationSettingRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.notification.NotificationService;
import com.sep490.anomaly_training_backend.service.notification.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
// FIX: Bỏ @ConditionalOnProperty ở class level.
// sendNotification() cần RabbitMQ → guard bên trong method.
// dispatchNudgeEmails() gửi thẳng SMTP → không cần RabbitMQ, không nên bị tắt theo.
public class NotificationServiceImpl implements NotificationService {

    private static final int SLA_DEADLINE_DAYS = 3;

    private final NotificationProducer notificationProducer;
    private final NotificationSettingRepository settingsRepository;
    private final NotificationTemplateService notificationTemplateService;

    private final MailDispatcher mailDispatcher;

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingResultRepository trainingResultRepository;
    private final DefectProposalRepository defectProposalRepository;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
    private final UserRepository userRepository;
    private final ApprovalActionRepository approvalActionRepository;

    // ── Public API ───────────────────────────────────────────────────────────

    @Override
    public void sendNotification(NotificationRequest request) {
        if (!isNotificationEnabled(request.getType())) {
            log.info("Notification type {} is disabled, skipping", request.getType());
            return;
        }
        if (request.getCorrelationId() == null) {
            request.setCorrelationId(UUID.randomUUID().toString());
        }
        log.info("Queueing notification - Type: {}, Recipient: {}, CorrelationId: {}",
                request.getType(), request.getRecipientEmail(), request.getCorrelationId());
        notificationProducer.sendNotification(request);
    }

    @Override
    public void sendNotification(NotificationType type, User recipient,
                                 Map<String, Object> variables,
                                 Long relatedEntityId, String relatedEntityTable) {
        NotificationRequest request = NotificationRequest.builder()
                .type(type)
                .recipientUserId(recipient.getId())
                .recipientEmail(recipient.getEmail())
                .recipientName(recipient.getFullName())
                .variables(variables)
                .relatedEntityId(relatedEntityId)
                .relatedEntityTable(relatedEntityTable)
                .channel(NotificationChannel.EMAIL)
                .build();
        sendNotification(request);
    }

    @Override
    public void sendNotificationToMultiple(NotificationType type, List<User> recipients,
                                           Map<String, Object> variables,
                                           Long relatedEntityId, String relatedEntityTable) {
        for (User recipient : recipients) {
            sendNotification(type, recipient, variables, relatedEntityId, relatedEntityTable);
        }
    }

    @Override
    public boolean isNotificationEnabled(NotificationType type) {
        return settingsRepository.findEnabledByTemplateCode(type.name())
                .map(NotificationSetting::getIsEnabled)
                .orElse(true);
    }

    // ── Nudge (nhắc ký thủ công) ─────────────────────────────────────────────

    @Override
    public void sendReminderNotificationManually(ApprovalEntityType entityType, Long entityId) {
        User sender = getCurrentUser();

        switch (entityType) {
            case TRAINING_PLAN -> trainingPlanRepository.findById(entityId)
                    .ifPresentOrElse(
                            plan -> sendNudgeForTrainingPlan(plan, sender),
                            () -> {
                                throw new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND);
                            });

            case TRAINING_RESULT -> trainingResultRepository.findById(entityId)
                    .ifPresentOrElse(
                            result -> sendNudgeForTrainingResult(result, sender),
                            () -> {
                                throw new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND);
                            });

            case DEFECT_PROPOSAL -> defectProposalRepository.findById(entityId)
                    .ifPresentOrElse(
                            proposal -> sendNudgeForDefectProposal(proposal, sender),
                            () -> {
                                throw new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND);
                            });

            case TRAINING_SAMPLE_PROPOSAL -> trainingSampleProposalRepository.findById(entityId)
                    .ifPresentOrElse(
                            proposal -> sendNudgeForTrainingSampleProposal(proposal, sender),
                            () -> {
                                throw new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND);
                            });
        }
    }

    // ── Nudge handlers từng loại entity ─────────────────────────────────────

    private void sendNudgeForTrainingPlan(TrainingPlan plan, User sender) {
        validateWaitingStatus(String.valueOf(plan.getStatus()), ApprovalEntityType.TRAINING_PLAN, plan.getId());

        Map<String, Object> baseVars = buildEntityVars(
                ApprovalEntityType.TRAINING_PLAN,
                plan.getFormCode(),
                plan.getTitle(),
                plan.getLine() != null ? plan.getLine().getName() : "—",
                String.valueOf(plan.getStatus()),
                resolveSubmittedAt(ApprovalEntityType.TRAINING_PLAN, plan.getId())
        );

        dispatchNudgeEmails(
                resolveApprovers(String.valueOf(plan.getStatus())),
                sender, baseVars,
                "[Nhắc ký] Kế hoạch huấn luyện: " + plan.getTitle(),
                ApprovalEntityType.TRAINING_PLAN, plan.getId()
        );
    }

    private void sendNudgeForTrainingResult(TrainingResult result, User sender) {
        validateWaitingStatus(String.valueOf(result.getStatus()), ApprovalEntityType.TRAINING_RESULT, result.getId());

        Map<String, Object> baseVars = buildEntityVars(
                ApprovalEntityType.TRAINING_RESULT,
                result.getFormCode(),
                result.getTitle(),
                result.getLine() != null ? result.getLine().getName() : "—",
                String.valueOf(result.getStatus()),
                resolveSubmittedAt(ApprovalEntityType.TRAINING_RESULT, result.getId())
        );

        dispatchNudgeEmails(
                resolveApprovers(String.valueOf(result.getStatus())),
                sender, baseVars,
                "[Nhắc ký] Kết quả huấn luyện: " + result.getTitle(),
                ApprovalEntityType.TRAINING_RESULT, result.getId()
        );
    }

    private void sendNudgeForDefectProposal(DefectProposal proposal, User sender) {
        validateWaitingStatus(String.valueOf(proposal.getStatus()), ApprovalEntityType.DEFECT_PROPOSAL, proposal.getId());

        Map<String, Object> baseVars = buildEntityVars(
                ApprovalEntityType.DEFECT_PROPOSAL,
                proposal.getFormCode(),
                "Báo cáo lỗi – " + proposal.getFormCode(),
                proposal.getProductLine() != null ? proposal.getProductLine().getName() : "—",
                String.valueOf(proposal.getStatus()),
                resolveSubmittedAt(ApprovalEntityType.DEFECT_PROPOSAL, proposal.getId())
        );

        dispatchNudgeEmails(
                resolveApprovers(String.valueOf(proposal.getStatus())),
                sender, baseVars,
                "[Nhắc ký] Báo cáo lỗi: " + proposal.getFormCode(),
                ApprovalEntityType.DEFECT_PROPOSAL, proposal.getId()
        );
    }

    private void sendNudgeForTrainingSampleProposal(TrainingSampleProposal proposal, User sender) {
        validateWaitingStatus(String.valueOf(proposal.getStatus()),
                ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL, proposal.getId());

        Map<String, Object> baseVars = buildEntityVars(
                ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL,
                proposal.getFormCode(),
                "Chủ đề đào tạo – " + proposal.getFormCode(),
                proposal.getProductLine() != null ? proposal.getProductLine().getName() : "—",
                String.valueOf(proposal.getStatus()),
                resolveSubmittedAt(ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL, proposal.getId())
        );

        dispatchNudgeEmails(
                resolveApprovers(String.valueOf(proposal.getStatus())),
                sender, baseVars,
                "[Nhắc ký] Chủ đề đào tạo: " + proposal.getFormCode(),
                ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL, proposal.getId()
        );
    }

    // ── Core dispatch ────────────────────────────────────────────────────────

    private void dispatchNudgeEmails(List<User> approvers, User sender,
                                     Map<String, Object> baseVars, String subject,
                                     ApprovalEntityType entityType, Long entityId) {
        if (approvers.isEmpty()) {
            log.warn("[NudgeMail] Không tìm thấy approver cho {} id={}", entityType, entityId);
            return;
        }

        for (User approver : approvers) {
            try {
                Map<String, Object> vars = new HashMap<>(baseVars);
                vars.putAll(buildRecipientVars(approver));
                vars.putAll(buildSenderVars(sender));
                vars.put("approvalLink", buildApprovalLink(entityType, entityId));

                // FIX: Dùng notificationTemplateService.renderBody() thay vì gọi
                // emailTemplateService trực tiếp — tránh duplicate logic và dùng đúng code.
                String htmlBody = notificationTemplateService.renderBody(NotificationType.APPROVAL_NUDGE.toString(), vars);
                mailDispatcher.sendWithDbTemplate(approver.getEmail(), NotificationType.APPROVAL_NUDGE.toString(), vars);

                log.info("[NudgeMail] Đã gửi nhắc ký tới {} <{}> cho {} id={}",
                        approver.getFullName(), approver.getEmail(), entityType, entityId);

            } catch (Exception ex) {
                log.error("[NudgeMail] Gửi thất bại tới {} cho {} id={}: {}",
                        approver.getEmail(), entityType, entityId, ex.getMessage());
            }
        }
    }

    // ── Build variables ──────────────────────────────────────────────────────

    private Map<String, Object> buildEntityVars(ApprovalEntityType entityType,
                                                String formCode,
                                                String documentTitle,
                                                String productLineName,
                                                String entityStatus,
                                                LocalDateTime submittedAt) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("entityTypeLabel", entityTypeLabel(entityType));
        vars.put("entityTypeCssClass", entityCssClass(entityType));
        vars.put("formCode", formCode);
        vars.put("documentTitle", documentTitle);
        vars.put("productLineName", productLineName);
        vars.put("submittedAt", submittedAt);
        vars.put("waitingHours", Duration.between(submittedAt, LocalDateTime.now()).toHours());

        boolean waitingSv = entityStatus != null && entityStatus.contains("WAITING_SV");
        vars.put("currentStatusLabel", waitingSv ? "Chờ Giám sát duyệt" : "Chờ Quản lý duyệt");
        vars.put("statusCssClass", waitingSv ? "status-waiting-sv" : "status-waiting-mg");
        vars.put("slaDeadlineDays", SLA_DEADLINE_DAYS);
        vars.put("hasSenderMessage", false);
        vars.put("senderMessage", "");
        return vars;
    }

    private Map<String, Object> buildRecipientVars(User approver) {
        String role = approver.getRoles().stream()
                .findFirst()
                .map(r -> switch (r.getRoleCode()) {
                    case "ROLE_SUPERVISOR" -> "Giám sát";
                    case "ROLE_MANAGER" -> "Quản lý";
                    default -> r.getDisplayName();
                })
                .orElse("Phê duyệt viên");

        return Map.of(
                "recipientName", approver.getFullName(),
                "recipientRole", role
        );
    }

    private Map<String, Object> buildSenderVars(User sender) {
        String teamRole = sender.getRoles().stream()
                .findFirst()
                .map(Role::getDisplayName)
                .orElse("Trưởng tổ");

        return Map.of(
                "senderName", sender.getFullName(),
                "senderRole", teamRole,
                "senderInitials", initials(sender.getFullName())
        );
    }

    // ── Lookup helpers ───────────────────────────────────────────────────────

    private List<User> resolveApprovers(String entityStatus) {
        if (entityStatus == null) return List.of();
        return userRepository.findAllUsersWithRoles().stream()
                .filter(user ->
                        (entityStatus.contains("WAITING_SV") && user.hasRole("ROLE_SUPERVISOR"))
                                || (entityStatus.contains("WAITING_MANAGER") && user.hasRole("ROLE_MANAGER")))
                .toList();
    }

    private LocalDateTime resolveSubmittedAt(ApprovalEntityType entityType, Long entityId) {
        // FIX: Instant.from(LocalDateTime) ném DateTimeException vì thiếu timezone.
        // Dùng .atZone(ZoneId.systemDefault()).toLocalDateTime() để convert đúng.
        return approvalActionRepository
                .findByEntityTypeAndEntityIdOrderByPerformedAtAsc(entityType, entityId)
                .stream()
                .findFirst()
                .map(ApprovalActionLog::getPerformedAt)
                .map(instant -> instant.atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.now().minusHours(1));
    }

    private void validateWaitingStatus(String status, ApprovalEntityType entityType, Long entityId) {
        if (status == null
                || (!status.contains("WAITING_SV") && !status.contains("WAITING_MANAGER"))) {
            log.warn("[NudgeMail] {} id={} có status='{}' – không ở trạng thái chờ duyệt, bỏ qua.",
                    entityType, entityId, status);
            throw new AppException(ErrorCode.APPROVAL_STEP_NOT_FOUND);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String buildApprovalLink(ApprovalEntityType entityType, Long entityId) {
        String path = switch (entityType) {
            case TRAINING_PLAN -> "training-plans";
            case TRAINING_RESULT -> "training-results";
            case DEFECT_PROPOSAL -> "defect-proposals";
            case TRAINING_SAMPLE_PROPOSAL -> "training-sample-proposals";
            case TRAINING_SAMPLE_REVIEW -> "training-sample-reviews";
        };
        return "/" + path + "/" + entityId;
    }

    private String entityTypeLabel(ApprovalEntityType type) {
        return switch (type) {
            case TRAINING_PLAN -> "Kế hoạch huấn luyện";
            case TRAINING_RESULT -> "Kết quả huấn luyện";
            case DEFECT_PROPOSAL -> "Báo cáo lỗi";
            case TRAINING_SAMPLE_PROPOSAL -> "Chủ đề đào tạo";
            case TRAINING_SAMPLE_REVIEW -> "Kiểm tra hàng năm";
        };
    }

    private String entityCssClass(ApprovalEntityType type) {
        return switch (type) {
            case TRAINING_PLAN -> "type-plan";
            case TRAINING_RESULT -> "type-result";
            case DEFECT_PROPOSAL -> "type-defect";
            case TRAINING_SAMPLE_PROPOSAL -> "type-sample";
            case TRAINING_SAMPLE_REVIEW -> "type-review";
        };
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].charAt(0) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}