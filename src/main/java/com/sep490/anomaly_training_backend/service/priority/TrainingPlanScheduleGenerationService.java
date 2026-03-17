package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.model.TrainingPlan;

import java.time.LocalDate;
import java.util.Map;

public interface TrainingPlanScheduleGenerationService {
    TrainingPlan generateOptimalSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear);

    TrainingPlan regenerateSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear);

    int getAvailableSlots(Long trainingPlanId, LocalDate date);

    ScheduleSummary getScheduleSummary(Long trainingPlanId);

    @lombok.Data
    @lombok.Builder
    public static class ScheduleSummary {
        private int totalSlots;
        private int totalDays;
        private double avgSlotsPerDay;
        private Map<LocalDate, Long> countByDate;
        private Map<TrainingPlanDetailStatus, Long> countByStatus;
    }
}
