package com.sep490.anomaly_training_backend.service.notification;

import java.util.Map;

public interface EmailTemplateService {
    String renderTemplate(String templateName, Map<String, Object> variables);
}
