package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class UpdateResultDetailRequest {
    private Long id; // ID của training_result_detail

    // Chọn Công đoạn (dropdown)
    private Long processId;

    // Chọn Mã sản phẩm (dropdown / text)
    private Long productId;

    // Chọn Hạng mục huấn luyện bất thường (dropdown)
    private Long trainingSampleId;

    // Số quản lý mẫu
    private String sampleCode;

    // Phân loại (auto-fill từ process, nhưng có thể override)
    private Integer classification;

    // Thời gian vòng thao tác TB (s) - auto-fill từ process, nhưng có thể override
    private BigDecimal cycleTimeStandard;


    // Thời gian đưa mẫu vào
    private LocalTime timeIn;

    // Thời gian bắt đầu vòng thao tác
    private LocalTime timeStartOp;

    // Thời gian lấy mẫu ra
    private LocalTime timeOut;

    // Đánh giá: Đạt/Trượt
    private Boolean isPass;

    // Thời gian phát hiện (detection)
    private Integer detectionTime;

    // Ghi chú
    private String note;

    // Ký: Pro ký vào/ra
    private Boolean isSignProIn;
    private Boolean isSignProOut;

    // Tùy chọn: Huấn luyện lại
    private Boolean isRetrained;

    // Training topic (text tự do nếu không chọn sample)
    private String trainingTopic;
}