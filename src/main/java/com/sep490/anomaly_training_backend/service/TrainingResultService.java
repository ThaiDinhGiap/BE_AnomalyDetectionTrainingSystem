package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.model.TrainingResult;

import java.util.List;

public interface TrainingResultService {
    public TrainingResult generateTrainingResult(Long planId);
    public List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId);
    public List<TrainingResultOptionResponse> getTrainingTopicsByProcess(Long processId);
    public void updateResult(UpdateTrainingResultRequest request);
    public void signDetailsByFi(List<FiSignRequest> requests);
    public List<TrainingResultListResponse> getAllTrainingResults();
    public TrainingResultDetailResponse getTrainingResultDetail(Long id);
    public void submitResult(Long resultId);
}
