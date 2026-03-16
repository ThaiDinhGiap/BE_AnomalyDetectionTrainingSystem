package com.sep490.anomaly_training_backend.dto.response;

import com.sep490.anomaly_training_backend.dto.scoring.PrioritySnapshotResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrainingPlanGenerationResponse {
    TrainingPlanResponse trainingPlan;
    PrioritySnapshotResponse prioritySnapshot;
}
