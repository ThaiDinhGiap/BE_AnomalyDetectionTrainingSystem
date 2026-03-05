package com.sep490.anomaly_training_backend.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhóm các detail theo batchId.
 * Cùng 1 employee có thể xuất hiện nhiều lần (nhiều row trên FE),
 * mỗi lần thêm có 1 batchId riêng.
 */
@Data
public class EmployeePlanGroup {
    private String batchId;

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    // Tất cả process mà employee có skill (thuộc product line của plan)
    private List<TrainingPlanDetailResponse.ProcessInfo> employeeProcesses;

    // Danh sách các detail (mỗi detail = 1 ngày huấn luyện)
    private List<TrainingPlanDetailResponse> schedules = new ArrayList<>();
}

