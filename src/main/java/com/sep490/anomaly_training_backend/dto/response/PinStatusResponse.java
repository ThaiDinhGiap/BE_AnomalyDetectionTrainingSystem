package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PinStatusResponse {
    private boolean hasPin;
    private boolean isLocked;
    private boolean isExpired;
    private Instant expiresAt;
    private Instant lockedUntil;
    private Integer failedAttempts;
}