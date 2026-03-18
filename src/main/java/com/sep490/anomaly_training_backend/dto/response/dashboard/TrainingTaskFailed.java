package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTaskFailed {
    private Long id;
    private Long planId;
    private Long resultId;
    private String employeeName;
    private String employeeCode;
    private String processName;
    private String date;
    private String reason;
    private String action;
}
