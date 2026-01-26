package com.sep490.anomaly_training_backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailNotificationMessage implements Serializable {
    private String templateCode;
    private Long recipientUserId;
    private String recipientEmail;
    private String recipientName;
    private Map<String, Object> variables;
    private Integer priority; // 1-10, cao nhất là 1
    private Integer retryCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Metadata
    private String correlationId; // Để trace
    private String source; // "PLAN_APPROVAL", "TRAINING_REMINDER", etc.
}