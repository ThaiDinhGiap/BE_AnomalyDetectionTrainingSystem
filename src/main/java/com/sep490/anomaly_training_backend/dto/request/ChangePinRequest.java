package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePinRequest {

    @NotBlank(message = "PIN cũ không được để trống")
    private String oldPin;

    @NotBlank(message = "PIN mới không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "PIN phải gồm 6 chữ số")
    private String newPin;

    @NotBlank(message = "Xác nhận PIN không được để trống")
    private String confirmNewPin;
}