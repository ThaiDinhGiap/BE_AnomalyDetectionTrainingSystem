package com.sep490.anomaly_training_backend.service.sample;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewConfigResponse;

import java.util.List;

public interface TrainingSampleReviewConfigService {
    List<TrainingSampleReviewConfigResponse> getTrainingSampleReviewConfigByProductLine(Long productLineId);
}
