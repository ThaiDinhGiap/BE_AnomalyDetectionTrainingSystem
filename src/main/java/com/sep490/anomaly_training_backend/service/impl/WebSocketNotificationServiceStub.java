package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.service.WebSocketNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.websocket.enabled",
        havingValue = "false",
        matchIfMissing = true
)
@Slf4j
public class WebSocketNotificationServiceStub implements WebSocketNotificationService {

    @Override
    public void pushToUser(Long recipientId, InAppNotificationDto dto) {
        log.debug("[WebSocket stub] userId={} | type={} | title='{}'",
                recipientId, dto.getType(), dto.getTitle());
    }
}
