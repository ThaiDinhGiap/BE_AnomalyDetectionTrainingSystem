package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.NotificationTemplate;
import com.sep490.anomaly_training_backend.repository.NotificationTemplateRepository;
import com.sep490.anomaly_training_backend.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    public NotificationTemplate getTemplateByCode(String code) {
        return notificationTemplateRepository.findById(code)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
    }

    @Override
    public String renderSubject(String code, Object context) {
        return "";
    }

    @Override
    public String renderBody(String code, Object context) {
        return "";
    }
}