package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class TrainingResultDetailResponse {
    // Header info
    private Long id;
    private String title;
    private String status;
    private Integer currentVersion;
    private String note;

    // Line / Group info
    private Long lineId;
    private String lineName;
    private Long groupId;
    private String groupName;

    // Plan info
    private Long trainingPlanId;
    private String trainingPlanTitle;

    // Metadata
    private String createdByName;
    private LocalDateTime createdAt;
    private Integer year;

    private List<DetailRowDto> details;

    @Data
    public static class DetailRowDto {
        private Long id;

        // Plan detail info
        private Long trainingPlanDetailId;
        private String batchId;
        private LocalDate plannedDate;
        private LocalDate actualDate;

        // Employee info
        private Long employeeId;
        private String employeeName;
        private String employeeCode;

        // Process info (Công đoạn - dropdown)
        private Long processId;
        private String processCode;
        private String processName;

        // Phân loại (MS.1.2 format)
        private String classification;

        // Thời gian vòng thao tác TB (s)
        private BigDecimal standardTime;

        // Product info (Mã sản phẩm - dropdown)
        private Long productId;
        private String productCode;
        private String productName;

        // Hạng mục huấn luyện bất thường (dropdown)
        private Long trainingSampleId;
        private String trainingSampleName;

        // Số quản lý mẫu
        private String sampleCode;

        // Training topic (text tự do)
        private String trainingTopic;

        // Time tracking
        private LocalTime timeIn;       // Thời gian đưa mẫu vào
        private LocalTime timeStartOp;  // Thời gian bắt đầu vòng thao tác
        private LocalTime timeOut;      // Thời gian lấy mẫu ra
        private Integer detectionTime;

        // Result
        private Boolean isPass;         // Đánh giá: Đạt/Trượt
        private Boolean isRetrained;    // Huấn luyện lại
        private String note;            // Ghi chú
        private String detailStatus;

        // Signatures
        private Long signatureProInId;      // Chữ ký Pro vào
        private String signatureProInName;
        private Long signatureFiInId;       // FI xác nhận đưa mẫu vào
        private String signatureFiInName;
        private Long signatureProOutId;     // Chữ ký Pro ra
        private String signatureProOutName;
        private Long signatureFiOutId;      // FI xác nhận lấy mẫu ra
        private String signatureFiOutName;
    }
}
