package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.messaging.producer.NotificationProducer;
import com.sep490.anomaly_training_backend.model.NotificationSetting;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.NotificationSettingRepository;
import com.sep490.anomaly_training_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class NotificationServiceImpl implements NotificationService {

    private final NotificationProducer notificationProducer;
    private final NotificationSettingRepository settingsRepository;

    @Override
    public void sendNotification(NotificationRequest request) {
        // Check if notification is enabled
        if (!isNotificationEnabled(request.getType())) {
            log.info("Notification type {} is disabled, skipping", request.getType());
            return;
        }

        // Set correlation ID if not set
        if (request.getCorrelationId() == null) {
            request.setCorrelationId(UUID.randomUUID().toString());
        }

        log.info("Queueing notification - Type: {}, Recipient: {}, CorrelationId: {}",
                request.getType(), request.getRecipientEmail(), request.getCorrelationId());

        notificationProducer.sendNotification(request);
    }

    @Override
    public void sendNotification(NotificationType type, User recipient, Map<String, Object> variables,
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
                                           Map<String, Object> variables, Long relatedEntityId, String relatedEntityTable) {
        for (User recipient : recipients) {
            sendNotification(type, recipient, variables, relatedEntityId, relatedEntityTable);
        }
    }

    @Override
    public boolean isNotificationEnabled(NotificationType type) {
        return settingsRepository.findEnabledByTemplateCode(type.name())
                .map(NotificationSetting::getIsEnabled)
                .orElse(true); // Default:  enabled
    }
}