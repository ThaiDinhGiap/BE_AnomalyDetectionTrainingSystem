package com.sep490.anomaly_training_backend.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TrainingPlanUpdateRequest {
    private String title;
    private String note;

    private List<TrainingPlanDetailRequest> details;
}