package com.denso.anomaly_training_backend.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TrainingPlanRequest {
    private Long id;
    private String title;
    private LocalDate monthStart;
    private LocalDate monthEnd;
    private Long groupId;

    private Long supervisorId;

    private List<TrainingPlanDetailRequest> details;
}