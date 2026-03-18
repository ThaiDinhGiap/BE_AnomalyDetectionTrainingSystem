package com.sep490.anomaly_training_backend.dto.response.dashboard;

import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTaskToday {
    private Long id;
    private String employeeName;
    private String employeeCode;
    private List<ProcessInfo> employeeProcesses;

    @Data
    public static class ProcessInfo {
        private Long id;
        private String name;
    }
    private String timeSlot;    // "14:00 - 15:00"
}
