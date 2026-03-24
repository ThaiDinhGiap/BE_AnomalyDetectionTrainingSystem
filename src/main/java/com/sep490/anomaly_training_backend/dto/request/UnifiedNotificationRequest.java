package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object cho UnifiedNotificationDispatcher.
 * Dùng builder pattern để tránh method signature với nhiều tham số.
 *
 * <pre>{@code
 * UnifiedNotificationRequest.builder()
 *     .recipientUserId(userId)
 *     .recipientEmail(email)   // null → skip email
 *     .title("Kế hoạch được duyệt")
 *     .message("Kế hoạch Q1/2026 đã được Supervisor phê duyệt.")
 *     .type(InAppNotificationType.SUCCESS)
 *     .relatedEntityType("TRAINING_PLAN")
 *     .relatedEntityId(planId)
 *     .actionUrl("/training-plans/" + planId)
 *     .sendEmail(true)
 *     .sendInApp(true)
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedNotificationRequest {

    // Recipient
    Long recipientUserId;
    String recipientEmail;

    // Email fields
    String emailSubject;
    String emailHtmlBody;
    String emailTemplateCode;
    java.util.Map<String, Object> emailTemplateVars;

    // In-app fields
    String title;
    String message;

    @Builder.Default
    InAppNotificationType type = InAppNotificationType.INFO;

    String relatedEntityType;
    Long relatedEntityId;
    String actionUrl;

    // Channel switches
    @Builder.Default
    boolean sendEmail = false;

    @Builder.Default
    boolean sendInApp = true;
}
