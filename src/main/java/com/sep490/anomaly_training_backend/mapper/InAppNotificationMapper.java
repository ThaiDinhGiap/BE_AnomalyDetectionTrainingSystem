package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.model.InAppNotification;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationMapper {

    public InAppNotificationDto toDto(InAppNotification n) {
        if (n == null) return null;

        return InAppNotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .relatedEntityType(n.getRelatedEntityType())
                .relatedEntityId(n.getRelatedEntityId())
                .actionUrl(n.getActionUrl())
                .build();
    }
}
