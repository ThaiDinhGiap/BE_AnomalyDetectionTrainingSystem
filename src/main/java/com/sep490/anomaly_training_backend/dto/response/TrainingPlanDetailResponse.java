package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainingPlanDetailResponse {
    private Long id;

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private Long processId;
    private String processName;

    private LocalDate targetMonth;
    private LocalDate plannedDate;

    private ReportStatus status;
    private String note;
}