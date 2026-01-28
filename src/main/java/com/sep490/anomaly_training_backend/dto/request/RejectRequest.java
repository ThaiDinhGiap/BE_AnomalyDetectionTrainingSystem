package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequest {
    @NotBlank(message = "PIN không được để trống")
    private String pin;

    @NotBlank(message = "Lý do từ chối không được để trống")
    private String rejectReason;

    private String comment;
}