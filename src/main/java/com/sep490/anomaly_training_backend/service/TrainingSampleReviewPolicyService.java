package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewPolicyResponse;

import java.util.List;

public interface TrainingSampleReviewPolicyService {
    List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId);
}
