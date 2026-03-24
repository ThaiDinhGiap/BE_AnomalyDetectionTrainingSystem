package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InAppNotificationService {

    InAppNotificationDto send(SendInAppNotificationRequest request);

    List<InAppNotificationDto> sendToMany(List<Long> recipientIds, SendInAppNotificationRequest template);

    List<InAppNotificationDto> getUnread(Long recipientId);

    Page<InAppNotificationDto> getAll(Long recipientId, Pageable pageable);

    long countUnread(Long recipientId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long recipientId);

    void delete(Long notificationId, Long recipientId);
}
