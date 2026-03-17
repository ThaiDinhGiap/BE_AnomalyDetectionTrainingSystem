package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface TrainingSampleReviewPolicyService {
    List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId);

    TrainingSampleReviewPolicyResponse createNewReviewPolicy(TrainingSampleReviewPolicyRequest request);

    List<TrainingSampleReviewResponse> findByConfigId(Long configId);

    List<TrainingSampleReviewResponse> findByProductLine(Long productLineId);

    void deletePolicy(Long policyId);

    TrainingSampleReviewResponse assignTeamLeadToReview(TrainingSampleReviewRequest request);

    TrainingSampleReviewResponse confirmReviewByTeamLead(TrainingSampleReviewRequest request);

    void approve(Long id, User currentUser,  ApproveRequest approveRequest, HttpServletRequest request);
}
