package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.model.NotificationTemplate;

public interface NotificationTemplateService {
    NotificationTemplate getTemplateByCode(String code);
    String renderSubject(String code, Object context);
    String renderBody(String code, Object context);
}
