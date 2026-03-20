package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TrainingPlanDetailResponse {
    private Long id;
    private String batchId;

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private LocalDate targetMonth;
    private LocalDate plannedDate;
    private LocalDate actualDate;

    private TrainingPlanDetailStatus status;
    private String note;
    private List<ProcessInfo> employeeProcesses;

    @Data
    public static class ProcessInfo {
        private Long id;
        private String name;
    }
}