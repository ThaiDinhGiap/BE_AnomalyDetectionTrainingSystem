package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTaskMissed {
    private Long id;
    private String employeeName;
    private String employeeCode;
    private List<TrainingTaskToday.ProcessInfo> employeeProcesses;

    @Data
    public static class ProcessInfo {
        private Long id;
        private String name;
    }
    private String date;
    private String reason;
    private String action;
}
