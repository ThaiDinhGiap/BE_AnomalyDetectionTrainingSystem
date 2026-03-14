package com.sep490.anomaly_training_backend.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrainingPlanSpecialDayRequest {
    Long id;
    Long trainingPlanId;
    LocalDate specialDay;
    Integer trainingSlot;
}
