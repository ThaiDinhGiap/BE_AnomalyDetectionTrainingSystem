package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.dto.ScheduleSummary;
import com.sep490.anomaly_training_backend.model.TrainingPlan;

import java.time.LocalDate;

public interface TrainingPlanScheduleGenerationService {
    TrainingPlan generateOptimalSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear);

    TrainingPlan regenerateSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear);

    int getAvailableSlots(Long trainingPlanId, LocalDate date);

    ScheduleSummary getScheduleSummary(Long trainingPlanId);
}