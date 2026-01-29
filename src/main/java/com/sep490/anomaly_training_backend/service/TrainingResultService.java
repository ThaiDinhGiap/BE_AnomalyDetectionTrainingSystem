package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;

import java.util.List;

public interface TrainingResultService {
    void generateTrainingResult(Long planId);

    List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId);

    List<TrainingResultOptionResponse> getTrainingTopicsByProcess(Long processId);

    void updateResult(UpdateTrainingResultRequest request);

    void signDetailsByFi(List<FiSignRequest> requests);

    List<TrainingResultListResponse> getAllTrainingResults();

    TrainingResultDetailResponse getTrainingResultDetail(Long id);

    void submitResult(Long resultId);
}
