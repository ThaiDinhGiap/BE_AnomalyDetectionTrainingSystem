package com.sep490.anomaly_training_backend.service.notification.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.NotificationTemplate;
import com.sep490.anomaly_training_backend.repository.NotificationTemplateRepository;
import com.sep490.anomaly_training_backend.service.notification.EmailTemplateService;
import com.sep490.anomaly_training_backend.service.notification.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final EmailTemplateService emailTemplateService;

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch template từ DB
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public NotificationTemplate getTemplateByCode(String code) {
        return notificationTemplateRepository.findById(code)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Render SUBJECT: lấy subjectTemplate từ DB, thay {placeholder} bằng variables
    //
    // VD subjectTemplate = "[Nhắc ký] {entityTypeLabel}: {documentTitle}"
    //    variables        = { "entityTypeLabel": "Kế hoạch huấn luyện",
    //                         "documentTitle":   "KH T3/2026 Tổ Tiện" }
    // → "[Nhắc ký] Kế hoạch huấn luyện: KH T3/2026 Tổ Tiện"
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String renderSubject(String code, Map<String, Object> variables) {
        NotificationTemplate template = getTemplateByCode(code);
        return replacePlaceholders(template.getSubjectTemplate(), variables);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Render BODY: lấy htmlTemplateName từ DB, Thymeleaf xử lý file HTML
    //
    // VD htmlTemplateName = "approval-nudge-request"
    //    → EmailTemplateService render file email/approval-nudge-request.html
    //      với toàn bộ th:text, th:if, th:each trong template
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String renderBody(String code, Map<String, Object> variables) {
        NotificationTemplate template = getTemplateByCode(code);
        return emailTemplateService.renderTemplate(template.getHtmlTemplateName(), variables);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Convenience: render cả subject + body cùng lúc
    // ─────────────────────────────────────────────────────────────────────────

    public RenderedNotification renderFull(String code, Map<String, Object> variables) {
        NotificationTemplate template = getTemplateByCode(code);
        return new RenderedNotification(
                replacePlaceholders(template.getSubjectTemplate(), variables),
                emailTemplateService.renderTemplate(template.getHtmlTemplateName(), variables)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Thay thế {key} trong subject template bằng giá trị từ variables map
    // ─────────────────────────────────────────────────────────────────────────

    private String replacePlaceholders(String template, Map<String, Object> variables) {
        if (template == null || variables == null) return template;
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}