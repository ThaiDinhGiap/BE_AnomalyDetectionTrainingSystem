package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.InAppNotificationMapper;
import com.sep490.anomaly_training_backend.model.InAppNotification;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.InAppNotificationRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.WebSocketNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InAppNotificationServiceImplTest {

    @Mock
    private InAppNotificationRepository notifyRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private InAppNotificationMapper mapper;
    @Mock
    private WebSocketNotificationService webSocket;

    @InjectMocks
    private InAppNotificationServiceImpl notificationService;

    private User recipient;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(1L);
    }

    @Test
    void send_ShouldSaveAndPushWebSocket() {
        SendInAppNotificationRequest req = SendInAppNotificationRequest.builder()
                .recipientId(1L)
                .title("Test Title")
                .message("Test Message")
                .type(InAppNotificationType.INFO)
                .build();

        when(userRepo.getReferenceById(1L)).thenReturn(recipient);
        
        InAppNotification savedNotification = new InAppNotification();
        savedNotification.setId(10L);
        when(notifyRepo.save(any(InAppNotification.class))).thenReturn(savedNotification);
        
        InAppNotificationDto dto = new InAppNotificationDto();
        dto.setId(10L);
        when(mapper.toDto(savedNotification)).thenReturn(dto);

        InAppNotificationDto result = notificationService.send(req);

        assertThat(result.getId()).isEqualTo(10L);
        verify(notifyRepo).save(any(InAppNotification.class));
        verify(webSocket).pushToUser(1L, dto);
    }

    @Test
    void sendToMany_ShouldSendToAllRecipients() {
        SendInAppNotificationRequest req = SendInAppNotificationRequest.builder()
                .title("Bulk Title")
                .message("Bulk Msg")
                .build();

        when(userRepo.getReferenceById(anyLong())).thenReturn(recipient);
        when(notifyRepo.save(any())).thenReturn(new InAppNotification());
        when(mapper.toDto(any())).thenReturn(new InAppNotificationDto());

        List<InAppNotificationDto> results = notificationService.sendToMany(List.of(1L, 2L), req);

        assertThat(results).hasSize(2);
        verify(webSocket, times(2)).pushToUser(anyLong(), any());
    }

    @Test
    void getUnread_ShouldReturnUnread() {
        when(notifyRepo.findUnreadByRecipientId(1L)).thenReturn(List.of(new InAppNotification()));
        when(mapper.toDto(any())).thenReturn(new InAppNotificationDto());

        List<InAppNotificationDto> unread = notificationService.getUnread(1L);

        assertThat(unread).hasSize(1);
    }

    @Test
    void getAll_ShouldReturnPage() {
        Page<InAppNotification> page = new PageImpl<>(List.of(new InAppNotification()));
        when(notifyRepo.findAllActiveByRecipientId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(mapper.toDto(any())).thenReturn(new InAppNotificationDto());

        Page<InAppNotificationDto> result = notificationService.getAll(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void countUnread_ShouldReturnCount() {
        when(notifyRepo.countUnreadByRecipientId(1L)).thenReturn(5L);
        assertThat(notificationService.countUnread(1L)).isEqualTo(5L);
    }

    @Test
    void markAsRead_ShouldUpdate() {
        when(notifyRepo.markAsRead(eq(10L), any())).thenReturn(1);
        notificationService.markAsRead(10L);
        verify(notifyRepo).markAsRead(eq(10L), any());
    }

    @Test
    void markAllAsRead_ShouldUpdate() {
        when(notifyRepo.markAllAsRead(eq(1L), any())).thenReturn(5);
        notificationService.markAllAsRead(1L);
        verify(notifyRepo).markAllAsRead(eq(1L), any());
    }

    @Test
    void delete_WhenSuccessful_ShouldSoftDelete() {
        when(notifyRepo.softDeleteByIdAndRecipient(10L, 1L)).thenReturn(1);
        notificationService.delete(10L, 1L);
        verify(notifyRepo).softDeleteByIdAndRecipient(10L, 1L);
    }

    @Test
    void delete_WhenNotFound_ShouldThrowAppException() {
        when(notifyRepo.softDeleteByIdAndRecipient(10L, 1L)).thenReturn(0);
        
        assertThatThrownBy(() -> notificationService.delete(10L, 1L))
                .isInstanceOf(AppException.class);
    }
}
