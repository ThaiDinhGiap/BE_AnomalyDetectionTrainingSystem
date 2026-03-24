package com.sep490.anomaly_training_backend.service.notification.impl;

import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.dto.request.UnifiedNotificationRequest;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Entry point duy nhất cho notification — gộp Email + In-App.
 *
 * <p>Caller không cần biết channel nào đang được dùng.
 * Chỉ build {@link UnifiedNotificationRequest} và gọi {@link #dispatch}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedNotificationDispatcher {

    private final MailDispatcher mailDispatcher;
    private final InAppNotificationService inAppService;

    /**
     * Dispatch notification theo channel được bật trong request.
     */
    public void dispatch(UnifiedNotificationRequest req) {

        // ── Email ──────────────────────────────────────────────────────────
        if (req.isSendEmail() && StringUtils.hasText(req.getRecipientEmail())) {
            sendEmail(req);
        }

        // ── In-App ─────────────────────────────────────────────────────────
        if (req.isSendInApp() && req.getRecipientUserId() != null) {
            sendInApp(req);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void sendEmail(UnifiedNotificationRequest req) {
        try {
            String subject = StringUtils.hasText(req.getEmailSubject())
                    ? req.getEmailSubject()
                    : req.getTitle();

            if (StringUtils.hasText(req.getEmailTemplateCode())) {
                mailDispatcher.sendWithDbTemplate(
                        req.getRecipientEmail(),
                        req.getEmailTemplateCode(),
                        req.getEmailTemplateVars()
                );
            } else if (StringUtils.hasText(req.getEmailHtmlBody())) {
                mailDispatcher.send(req.getRecipientEmail(), subject, req.getEmailHtmlBody());
            } else {
                mailDispatcher.send(req.getRecipientEmail(), subject, req.getMessage());
            }

            log.debug("[Unified] email sent to {}", req.getRecipientEmail());

        } catch (Exception e) {
            log.error("[Unified] email failed to={} | error={}", req.getRecipientEmail(), e.getMessage());
        }
    }

    private void sendInApp(UnifiedNotificationRequest req) {
        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(req.getRecipientUserId())
                        .title(req.getTitle())
                        .message(req.getMessage())
                        .type(req.getType())
                        .relatedEntityType(req.getRelatedEntityType())
                        .relatedEntityId(req.getRelatedEntityId())
                        .actionUrl(req.getActionUrl())
                        .build()
        );
    }
}
