package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.model.TrainingPlan;

public interface TrainingPlanScheduleGenerationService {
    TrainingPlan generateOptimalSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear);
}