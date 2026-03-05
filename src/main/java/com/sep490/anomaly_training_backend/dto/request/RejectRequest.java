package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RejectRequest {

    /**
     * IDs from reject_reasons table (at least one required).
     * Example: [1, 3] = "Thiếu thông tin mô tả lỗi chi tiết" + "Mẫu vật lý không đạt tiêu chuẩn"
     */
    @NotEmpty(message = "Please choose one reject reason at least")
    private List<Long> rejectReasonIds;

    /**
     * ID from required_actions table (required).
     */
    private Long requiredActionId;

    /**
     * Optional free-text details / additional feedback.
     */
    private String comment;
}