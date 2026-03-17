package com.sep490.anomaly_training_backend.service.notification.impl;

import com.sep490.anomaly_training_backend.service.notification.EmailTemplateService;
import com.sep490.anomaly_training_backend.service.notification.MailService;
import com.sep490.anomaly_training_backend.service.notification.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Điểm duy nhất để gửi mail trong toàn bộ hệ thống.
 * <p>
 * Các service khác chỉ cần inject MailDispatcher, không cần biết:
 * - Mail có đang bật không
 * - Template render từ DB hay file trực tiếp
 * - MailService hay EmailTemplateService
 * <p>
 * Có 3 cách dùng:
 * 1. send(to, subject, body)                           — gửi thẳng không template
 * 2. sendWithFile(to, subject, templateName, vars)     — render file Thymeleaf trực tiếp
 * 3. sendWithDbTemplate(to, templateCode, vars)        — render qua template lưu trong DB
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailDispatcher {

    // Optional vì MailServiceImpl có @ConditionalOnProperty
    // → khi MAIL_ENABLED=false bean không tồn tại, inject Optional thay vì crash
    private final Optional<MailService> mailService;
    private final EmailTemplateService emailTemplateService;
    private final NotificationTemplateService notificationTemplateService;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    // ── 1. Gửi thẳng, không template ────────────────────────────────────────

    /**
     * Gửi HTML email không cần template.
     * Dùng khi body đã được build sẵn trong code.
     */
    public void send(String to, String subject, String htmlBody) {
        dispatch(to, subject, htmlBody);
    }

    /**
     * Gửi HTML email có CC, không cần template.
     */
    public void sendWithCc(String to, String[] cc, String subject, String htmlBody) {
        if (!isAvailable()) return;
        try {
            mailService.get().sendMailWithCc(to, cc, subject, htmlBody);
        } catch (Exception e) {
            log.error("[Mail] Gửi thất bại tới {} (cc): {}", to, e.getMessage());
        }
    }

    // ── 2. Render từ file Thymeleaf trực tiếp ───────────────────────────────

    /**
     * Render template từ file rồi gửi.
     * templateName: tên file trong resources/templates/email/ (không cần .html)
     * <p>
     * Dùng khi template là file tĩnh, không quản lý qua DB.
     * VD: sendWithFile(to, "Xác nhận", "welcome", Map.of("name", "Minh"))
     */
    public void sendWithFile(String to, String subject,
                             String templateName, Map<String, Object> vars) {
        String body = emailTemplateService.renderTemplate(templateName, vars);
        dispatch(to, subject, body);
    }

    // ── 3. Render qua template lưu trong DB ─────────────────────────────────

    /**
     * Render template lấy từ DB theo code rồi gửi.
     * Subject cũng được render từ subjectTemplate trong DB.
     * <p>
     * Dùng khi subject và templateName được quản lý động qua DB.
     * VD: sendWithDbTemplate(to, NotificationType.APPROVAL_NUDGE_REQUEST.name(), vars)
     */
    public void sendWithDbTemplate(String to, String templateCode, Map<String, Object> vars) {
        String subject = notificationTemplateService.renderSubject(templateCode, vars);
        String body = notificationTemplateService.renderBody(templateCode, vars);
        dispatch(to, subject, body);
    }

    // ── Core ─────────────────────────────────────────────────────────────────

    private void dispatch(String to, String subject, String htmlBody) {
        if (!isAvailable()) return;
        try {
            mailService.get().sendHtmlMail(to, subject, htmlBody);
        } catch (Exception e) {
            log.error("[Mail] Gửi thất bại tới {}: {}", to, e.getMessage());
        }
    }

    /**
     * Kiểm tra mail có thể gửi không.
     * Log warn thay vì throw — tránh làm crash luồng nghiệp vụ chính vì lỗi mail.
     */
    private boolean isAvailable() {
        if (!mailEnabled || mailService.isEmpty()) {
            log.warn("[Mail] Mail đang tắt (MAIL_ENABLED=false), bỏ qua gửi mail.");
            return false;
        }
        return true;
    }
}