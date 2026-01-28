package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TrainingPlanDetailRequest {
    private Long employeeId;

    private Long processId;

    private String note; // Ghi chú cho dòng này

    // Danh sách các cột tháng (Ví dụ: Tháng 1, Tháng 2, Tháng 3)
    private List<ScheduleRequest> schedules;
}