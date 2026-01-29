package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

@Data
public class FiSignRequest {
    private Long id;            // ID của dòng detail
    private Boolean isSignIn;   // True: Ký xác nhận đầu vào
    private Boolean isSignOut;  // True: Ký xác nhận đầu ra
}