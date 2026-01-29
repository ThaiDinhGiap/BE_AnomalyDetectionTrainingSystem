package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportDetailResponse;

import java.util.List;

public interface TrainingTopicReportDetailService {
    List<TrainingTopicReportDetailResponse> getTrainingTopicReportDetails(Long trainingTopicReportId);
}
