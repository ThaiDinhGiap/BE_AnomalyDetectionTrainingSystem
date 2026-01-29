package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicResponse;

import java.util.List;

public interface TrainingTopicService {
    List<TrainingTopicResponse> getTrainingTopicsByGroup(Long groupId);
}
