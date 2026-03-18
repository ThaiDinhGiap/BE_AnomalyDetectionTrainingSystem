package com.sep490.anomaly_training_backend.dto.approval;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Cấu trúc JSON lưu vào cột reject_feedback (TEXT/JSON) của defect_proposal_details.
 * <p>
 * Ví dụ JSON được lưu:
 * {
 * "savedAt": "2026-03-12T08:30:00Z",
 * "savedBy": "Nguyễn Văn Giám Sát",
 * "rejectReasons": [
 * { "id": 1, "category": "DỮ LIỆU", "label": "Thiếu dữ liệu" },
 * { "id": 4, "category": "NỘI DUNG", "label": "Nội dung chưa đầy đủ" }
 * ],
 * "requiredAction": { "id": 1, "label": "Chỉnh sửa và gửi lại" },
 * "comment": "Thiếu ảnh minh chứng piston lắp sai hướng"
 * }
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RejectFeedbackJson {

    /**
     * Thời điểm lưu feedback
     */
    private Instant savedAt;

    /**
     * Tên người từ chối (snapshot)
     */
    private String savedBy;

    /**
     * Danh sách lý do đã chọn (snapshot label để hiển thị không cần join)
     */
    private List<RejectReasonSnapshot> rejectReasons;

    /**
     * Hành động yêu cầu (snapshot)
     */
    private RequiredActionSnapshot requiredAction;

    /**
     * Ghi chú tự do
     */
    private String comment;

    @Data
    @Builder
    public static class RejectReasonSnapshot {
        private Long id;
        private String category;
        private String label;
    }

    @Data
    @Builder
    public static class RequiredActionSnapshot {
        private Long id;
        private String label;
    }
}
