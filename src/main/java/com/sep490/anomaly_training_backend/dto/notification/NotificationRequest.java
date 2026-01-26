package com.sep490.anomaly_training_backend.dto.notification;

import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest implements Serializable {

    private String correlationId;

    // Notification type (template code)
    private NotificationType type;

    // Recipients
    private Long recipientUserId;
    private String recipientEmail;
    private String recipientName;

    // CC list (optional)
    private List<String> ccEmails;

    // Related entity info (for tracking)
    private Long relatedEntityId;
    private String relatedEntityTable; // training_plan, issue_report, etc.

    // Template variables
    private Map<String, Object> variables;

    // Channel
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.EMAIL;

    // Priority (0-10, higher = more important)
    @Builder.Default
    private int priority = 5;

    // Retry count
    @Builder.Default
    private int retryCount = 0;
}