package com.sep490.anomaly_training_backend.service.notification;

import com.sep490.anomaly_training_backend.model.NotificationTemplate;

import java.util.Map;

public interface NotificationTemplateService {
    NotificationTemplate getTemplateByCode(String code);

    String renderSubject(String code, Map<String, Object> variables);

    String renderBody(String code, Map<String, Object> variables);

    RenderedNotification renderFull(String code, Map<String, Object> variables);

    public record RenderedNotification(String subject, String htmlBody) {
    }
}
