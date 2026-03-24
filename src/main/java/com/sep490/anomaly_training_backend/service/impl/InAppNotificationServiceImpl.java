package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.InAppNotificationMapper;
import com.sep490.anomaly_training_backend.model.InAppNotification;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.InAppNotificationRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import com.sep490.anomaly_training_backend.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private final InAppNotificationRepository notifyRepo;
    private final UserRepository userRepo;
    private final InAppNotificationMapper mapper;
    private final WebSocketNotificationService webSocket;

    @Override
    @Transactional
    public InAppNotificationDto send(SendInAppNotificationRequest req) {
        User recipient = userRepo.getReferenceById(req.getRecipientId());

        InAppNotification n = InAppNotification.builder()
                .recipient(recipient)
                .title(req.getTitle())
                .message(req.getMessage())
                .type(req.getType())
                .relatedEntityType(req.getRelatedEntityType())
                .relatedEntityId(req.getRelatedEntityId())
                .actionUrl(req.getActionUrl())
                .build();

        InAppNotification saved = notifyRepo.save(n);
        InAppNotificationDto dto = mapper.toDto(saved);

        webSocket.pushToUser(req.getRecipientId(), dto);

        log.debug("[InApp] sent to userId={} | type={} | title='{}'",
                req.getRecipientId(), req.getType(), req.getTitle());

        return dto;
    }

    @Override
    @Transactional
    public List<InAppNotificationDto> sendToMany(List<Long> recipientIds, SendInAppNotificationRequest template) {
        return recipientIds.stream()
                .map(id -> send(
                        SendInAppNotificationRequest.builder()
                                .recipientId(id)
                                .title(template.getTitle())
                                .message(template.getMessage())
                                .type(template.getType())
                                .relatedEntityType(template.getRelatedEntityType())
                                .relatedEntityId(template.getRelatedEntityId())
                                .actionUrl(template.getActionUrl())
                                .build()
                ))
                .toList();
    }

    @Override
    public List<InAppNotificationDto> getUnread(Long recipientId) {
        return notifyRepo.findUnreadByRecipientId(recipientId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Page<InAppNotificationDto> getAll(Long recipientId, Pageable pageable) {
        return notifyRepo.findAllActiveByRecipientId(recipientId, pageable)
                .map(mapper::toDto);
    }

    @Override
    public long countUnread(Long recipientId) {
        return notifyRepo.countUnreadByRecipientId(recipientId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        int updated = notifyRepo.markAsRead(notificationId, LocalDateTime.now());
        if (updated == 0) {
            log.debug("[InApp] markAsRead no-op for id={}", notificationId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long recipientId) {
        int updated = notifyRepo.markAllAsRead(recipientId, LocalDateTime.now());
        log.debug("[InApp] markAllAsRead userId={} affected={}", recipientId, updated);
    }

    @Override
    @Transactional
    public void delete(Long notificationId, Long recipientId) {
        int deleted = notifyRepo.softDeleteByIdAndRecipient(notificationId, recipientId);
        if (deleted == 0) {
            throw new AppException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND,
                    "Notification not found or not owned by user");
        }
    }
}
