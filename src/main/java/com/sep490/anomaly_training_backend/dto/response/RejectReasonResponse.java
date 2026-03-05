package com.sep490.anomaly_training_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RejectReasonResponse {
    private Long id;
    private String reasonName;
}