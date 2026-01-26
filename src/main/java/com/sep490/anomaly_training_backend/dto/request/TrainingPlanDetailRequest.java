package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TrainingPlanDetailRequest {
    private Long id;
    private Long employeeId;
    private Long processId;
    private LocalDate targetMonth;
    private LocalDate plannedDate;
    private String note;
    private TrainingPlanDetailStatus resultStatus;
}