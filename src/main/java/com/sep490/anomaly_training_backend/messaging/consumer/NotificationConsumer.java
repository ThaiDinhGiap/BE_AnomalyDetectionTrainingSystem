package com.sep490.anomaly_training_backend.messaging.consumer;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.NotificationQueueStatus;
import com.sep490.anomaly_training_backend.messaging.config.RabbitMQConfig;
import com.sep490.anomaly_training_backend.model.NotificationQueue;
import com.sep490.anomaly_training_backend.model.NotificationTemplate;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.NotificationQueueRepository;
import com.sep490.anomaly_training_backend.repository.NotificationTemplateRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.MailService;
import com.sep490.anomaly_training_backend.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class NotificationConsumer {

    private final MailService mailService;
    private final NotificationTemplateService templateService;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationQueueRepository queueRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_COUNT = 3;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE, concurrency = "3-5")
    public void handleNotification(NotificationRequest request) {
        String correlationId = request.getCorrelationId();
        String typeCode = request.getType().name();

        try {
            log.info("Processing notification - Type: {}, Recipient: {}, CorrelationId: {}",
                    typeCode, request.getRecipientEmail(), correlationId);

            // 1. Load template
            NotificationTemplate template = templateRepository.findByCodeAndDeleteFlagFalse(typeCode)
                    .orElseThrow(() -> new RuntimeException("Template not found:  " + typeCode));

            // 2. Render subject và body
            String subject = templateService.renderSubject(template.getSubjectTemplate(), request.getVariables());
            String body = templateService.renderBody(template.getBodyTemplate(), request.getVariables());

            // 3. Gửi email
            if (request.getCcEmails() != null && !request.getCcEmails().isEmpty()) {
                mailService.sendMailWithCc(
                        request.getRecipientEmail(),
                        request.getCcEmails().toArray(new String[0]),
                        subject,
                        body
                );
            } else {
                mailService.sendHtmlMail(request.getRecipientEmail(), subject, body);
            }

            // 4. Lưu history - SUCCESS
            saveToQueue(request, template, subject, body, NotificationQueueStatus.SENT, null);

            log.info("Notification sent successfully - Type: {}, CorrelationId: {}", typeCode, correlationId);

        } catch (MailException e) {
            log.error("Mail error - Type: {}, CorrelationId: {}, Error: {}", typeCode, correlationId, e.getMessage());
            handleFailure(request, e);

        } catch (Exception e) {
            log.error("Unexpected error - Type: {}, CorrelationId: {}", typeCode, correlationId, e);
            handleFailure(request, e);
        }
    }

    private void handleFailure(NotificationRequest request, Exception error) {
        int retryCount = request.getRetryCount();

        if (retryCount < MAX_RETRY_COUNT) {
            request.setRetryCount(retryCount + 1);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RETRY_EXCHANGE,
                    RabbitMQConfig.RETRY_ROUTING_KEY,
                    request
            );

            log.warn("Notification queued for retry ({}/{}) - Type: {}, CorrelationId: {}",
                    retryCount + 1, MAX_RETRY_COUNT, request.getType(), request.getCorrelationId());

        } else {
            NotificationTemplate template = templateRepository.findByCodeAndDeleteFlagFalse(request.getType().name())
                    .orElse(null);
            saveToQueue(request, template, null, null, NotificationQueueStatus.FAILED, error.getMessage());

            log.error("Notification permanently failed after {} retries - Type: {}, CorrelationId: {}",
                    MAX_RETRY_COUNT, request.getType(), request.getCorrelationId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(NotificationRequest request) {
        log.error("Notification in DLQ - Type: {}, Recipient: {}, CorrelationId: {}",
                request.getType(), request.getRecipientEmail(), request.getCorrelationId());

        NotificationTemplate template = templateRepository.findByCodeAndDeleteFlagFalse(request.getType().name())
                .orElse(null);
        saveToQueue(request, template, null, null, NotificationQueueStatus.FAILED, "Moved to Dead Letter Queue");
    }

    private void saveToQueue(NotificationRequest request, NotificationTemplate template,
                             String subject, String body, NotificationQueueStatus status, String errorMessage) {
        try {
            String ccList = request.getCcEmails() != null
                    ? String.join(",", request.getCcEmails())
                    : null;

            // Fetch User entity from repository
            User recipientUser = null;
            if (request.getRecipientUserId() != null) {
                recipientUser = userRepository.findById(request.getRecipientUserId()).orElse(null);
            }

            NotificationQueue queue = NotificationQueue.builder()
                    .recipientUser(recipientUser)
                    .ccList(ccList)
                    .notificationTemplate(template)
                    .relatedEntityId(request.getRelatedEntityId())
                    .relatedEntityTable(request.getRelatedEntityTable())
                    .subject(subject != null ? subject : "N/A")
                    .body(body != null ? body : "N/A")
                    .status(status)
                    .retryCount(request.getRetryCount())
                    .errorMessage(errorMessage)
                    .scheduledAt(Instant.now())
                    .sentAt(status == NotificationQueueStatus.SENT ? Instant.now() : null)
                    .build();

            queueRepository.save(queue);
        } catch (Exception e) {
            log.error("Failed to save notification queue", e);
        }
    }
}