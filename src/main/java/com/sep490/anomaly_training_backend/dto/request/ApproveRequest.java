package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveRequest {
    @NotBlank(message = "PIN không được để trống")
    private String pin;

    private String comment;
}
