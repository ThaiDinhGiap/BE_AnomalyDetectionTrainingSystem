package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

@Data
public class FiSignRequest {
    private Long id;              // ID của dòng detail

    /**
     * Xác nhận đầu vào (đưa mẫu vào):
     * true = đồng ý, false = không đồng ý, null = bỏ qua (không ký)
     */
    private Boolean confirmIn;

    /**
     * Xác nhận đầu ra (lấy mẫu ra):
     * true = đồng ý, false = không đồng ý, null = bỏ qua (không ký)
     */
    private Boolean confirmOut;
}