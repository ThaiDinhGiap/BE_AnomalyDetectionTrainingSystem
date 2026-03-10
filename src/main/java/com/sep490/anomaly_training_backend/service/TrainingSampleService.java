package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;

import java.util.List;

public interface TrainingSampleService {
    List<TrainingSampleResponse> getTrainingSampleByProductLine(Long productLineId);

    TrainingSampleResponse getTrainingSampleById(Long id);

    List<TrainingSampleResponse> getTrainingSampleByProcess(Long id);

    List<TrainingSampleResponse> getTrainingSampleByCategory(Long id);

}
