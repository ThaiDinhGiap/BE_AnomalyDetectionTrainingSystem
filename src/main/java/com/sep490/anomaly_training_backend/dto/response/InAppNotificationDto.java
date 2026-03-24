package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InAppNotificationDto {

    Long id;
    String title;
    String message;
    InAppNotificationType type;   // FE tự map icon/màu từ field này
    Boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime readAt;

    // Deep-link
    String relatedEntityType;
    Long   relatedEntityId;
    String actionUrl;
}
