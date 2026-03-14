package com.sep490.anomaly_training_backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrainingPlanGenerationRequest {
    String title;
    LocalDate startDate;
    LocalDate endDate;
    Long lineId;
    Long teamId;
    Integer minTrainingPerDay;
    Integer maxTrainingPerDay;
    List<TrainingPlanSpecialDayRequest> specialDays;
}
