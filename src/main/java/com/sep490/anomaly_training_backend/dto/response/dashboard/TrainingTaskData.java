package com.sep490.anomaly_training_backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTaskData {
    private List<TrainingTaskToday> todayList;
    private List<TrainingTaskFailed> failedList;
    private List<TrainingTaskMissed> missedList;
}
