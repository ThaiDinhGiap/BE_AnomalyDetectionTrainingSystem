package com.sep490.anomaly_training_backend.messaging.producer;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationRequest request) {
        try {
            if (request.getCorrelationId() == null) {
                request.setCorrelationId(UUID.randomUUID().toString());
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    request,
                    message -> {
                        message.getMessageProperties().setPriority(request.getPriority());
                        message.getMessageProperties().setContentType("application/json");
                        message.getMessageProperties().setHeader("x-retry-count", request.getRetryCount());
                        message.getMessageProperties().setHeader("x-notification-type", request.getType().name());
                        return message;
                    }
            );

            log.info("Notification queued - Type: {}, Recipient: {}, CorrelationId: {}",
                    request.getType(), request.getRecipientEmail(), request.getCorrelationId());

        } catch (Exception e) {
            log.error("Failed to queue notification - Type: {}, Error: {}", request.getType(), e.getMessage());
            // TODO: Save to backup table for retry
        }
    }
}