package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTaskMissed {
    private Long id;
    private String employeeName;
    private String employeeCode;
    private String employeeProcesses; // Changed to String
    private String date;
    private String reason;
    private String action;
}
