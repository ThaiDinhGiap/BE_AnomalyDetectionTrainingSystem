package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;

/**
 * Gửi notification real-time tới browser qua WebSocket / SSE.
 *
 * <p>Có 2 implementation:</p>
 * <ul>
 *   <li>{@link WebSocketNotificationServiceStub}  — mặc định, chỉ log, không crash</li>
 *   <li>{@code WebSocketNotificationServiceImpl}  — bật khi {@code app.websocket.enabled=true}</li>
 * </ul>
 */
public interface WebSocketNotificationService {

    /**
     * Push notification tới một user cụ thể.
     *
     * @param recipientId ID của user nhận
     * @param dto         payload notification
     */
    void pushToUser(Long recipientId, InAppNotificationDto dto);
}


// ─── Stub (default) ──────────────────────────────────────────────────────────
// Đặt cùng file để tiện theo dõi; tách ra file riêng nếu project có convention khác.

/*
package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.service.WebSocketNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.websocket.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class WebSocketNotificationServiceStub implements WebSocketNotificationService {

    @Override
    public void pushToUser(Long recipientId, InAppNotificationDto dto) {
        log.debug("[WebSocket stub] Would push to userId={} | title='{}'", recipientId, dto.getTitle());
        // no-op — WebSocket chưa được cấu hình
    }
}
*/
