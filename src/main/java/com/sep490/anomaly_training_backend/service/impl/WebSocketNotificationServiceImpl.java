package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Real WebSocket implementation — active khi {@code app.websocket.enabled=true}.
 *
 * <p>Để bật: thêm vào {@code application.properties}:</p>
 * <pre>
 *   app.websocket.enabled=true
 * </pre>
 *
 * <p>Frontend kết nối và subscribe:</p>
 * <pre>{@code
 * // npm install @stomp/stompjs sockjs-client
 *
 * const client = new Client({
 *   webSocketFactory: () => new SockJS('/ws'),
 *   connectHeaders: { Authorization: 'Bearer ' + token },
 *   onConnect: () => {
 *     // Subscribe nhận notification của chính mình
 *     client.subscribe('/user/queue/notifications', (frame) => {
 *       const notif = JSON.parse(frame.body);
 *       // update bell badge, show toast, etc.
 *     });
 *   }
 * });
 * client.activate();
 * }</pre>
 */
@Service
@ConditionalOnProperty(name = "app.websocket.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push notification tới một user cụ thể.
     *
     * <p>Spring Security + STOMP tự map {@code recipientId} sang session của user đó
     * thông qua {@link org.springframework.messaging.simp.user.SimpUserRegistry}.
     * User phải đã authenticate và kết nối WebSocket thì mới nhận được.</p>
     *
     * <p>Nếu user chưa online — message bị drop (không queue lại).
     * Notification vẫn được lưu DB nên khi user login lại sẽ thấy qua REST API.</p>
     */
    @Override
    public void pushToUser(Long recipientId, InAppNotificationDto dto) {
        try {
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    "/queue/notifications",
                    dto
            );

            log.debug("[WebSocket] pushed to userId={} | type={} | title='{}'",
                    recipientId, dto.getType(), dto.getTitle());

        } catch (Exception e) {
            log.warn("[WebSocket] push failed for userId={} | error={}", recipientId, e.getMessage());
        }
    }
}