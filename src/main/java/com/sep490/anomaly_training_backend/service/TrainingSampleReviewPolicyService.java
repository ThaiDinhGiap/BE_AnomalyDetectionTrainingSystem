package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewResponse;

import java.util.List;

public interface TrainingSampleReviewPolicyService {
    List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId);

    TrainingSampleReviewPolicyResponse createNewReviewPolicy(TrainingSampleReviewPolicyRequest request);

    List<TrainingSampleReviewResponse> findByConfigId(Long configId);

    List<TrainingSampleReviewResponse> findByProductLine(Long productLineId);

    void deletePolicy(Long policyId);

    TrainingSampleReviewResponse assignTeamLeadToReview(TrainingSampleReviewRequest request);

    TrainingSampleReviewResponse confirmReviewByTeamLead(TrainingSampleReviewRequest request);

}
