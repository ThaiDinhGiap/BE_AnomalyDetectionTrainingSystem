package com.sep490.anomaly_training_backend.dto.approval;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DetailRejectFeedbackRequest {

    @NotEmpty(message = "Phải có ít nhất 1 feedback cho detail")
    @Valid
    private List<DetailFeedbackItem> feedbacks;

    @Data
    public static class DetailFeedbackItem {

        /**
         * ID của DefectProposalDetail cần gắn feedback
         */
        private Long detailId;

        /**
         * IDs từ bảng reject_reasons.
         * Null/empty = detail này không có vấn đề (bỏ qua).
         */
        private List<Long> rejectReasonIds;

        /**
         * ID từ bảng required_actions. Null = không yêu cầu hành động cụ thể
         */
        private Long requiredActionId;

        /**
         * Ghi chú thêm tự do
         */
        private String comment;
    }
}
