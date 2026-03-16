package com.sep490.anomaly_training_backend.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TrainingPlanUpdateRequest {

    // ===== HEADER (null = không thay đổi) =====
    private String title;
    private String note;
    private Long lineId;
    private LocalDate startDate;
    private LocalDate endDate;

    // ===== DETAILS (null = không thay đổi details) =====
    @Valid
    private List<DetailAction> details;

    @Data
    public static class DetailAction {
        /**
         * ADD          → thêm employee mới (tạo batchId mới, employeeId bắt buộc)
         * ADD_SCHEDULE → thêm ngày vào batch cũ (batchId bắt buộc, giữ nguyên row trên FE)
         * UPDATE       → cập nhật 1 detail cụ thể (detailId bắt buộc)
         * DELETE       → xóa detail (detailId bắt buộc)
         */
        private ActionType action;

        // Dùng cho UPDATE / DELETE
        private Long detailId;

        // Dùng cho ADD / ADD_SCHEDULE
        private Long employeeId;
        private List<ScheduleRequest> schedules;

        // Dùng cho ADD_SCHEDULE → giữ nguyên batch cũ (thêm ngày vào row cũ trên FE)
        private String batchId;

        // Dùng cho ADD / ADD_SCHEDULE / UPDATE
        private String note;

        // Dùng cho UPDATE (sửa trực tiếp 1 detail)
        private LocalDate targetMonth;
        private Integer plannedDay;
    }

    public enum ActionType {
        ADD, ADD_SCHEDULE, UPDATE, DELETE
    }
}