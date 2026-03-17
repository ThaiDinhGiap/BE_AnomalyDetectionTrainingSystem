package com.sep490.anomaly_training_backend.service.notification;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.model.User;

import java.util.Map;

public interface NotificationService {

    /**
     * Gửi notification (đưa vào queue)
     */
    void sendNotification(NotificationRequest request);

    /**
     * Helper:  Gửi notification cho user với variables
     */
    void sendNotification(NotificationType type, User recipient, Map<String, Object> variables,
                          Long relatedEntityId, String relatedEntityTable);

    /**
     * Helper: Gửi notification cho nhiều users
     */
    void sendNotificationToMultiple(NotificationType type, java.util.List<User> recipients,
                                    Map<String, Object> variables, Long relatedEntityId, String relatedEntityTable);

    /**
     * Kiểm tra notification có được bật không
     */
    boolean isNotificationEnabled(NotificationType type);

    void sendReminderNotificationManually(ApprovalEntityType entityType, Long entityId);
}