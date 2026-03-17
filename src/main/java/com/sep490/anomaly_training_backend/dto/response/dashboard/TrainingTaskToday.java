package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTaskToday {
    private Long id;
    private String employeeName;
    private String employeeCode;
    private String processName;
    private String type;        // "Định kỳ", "Bất thường (Lỗi)", "Đào tạo lại"
    private String timeSlot;    // "14:00 - 15:00"
}
