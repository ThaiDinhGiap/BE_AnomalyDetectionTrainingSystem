package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TrainingSampleReviewService {
    List<TrainingSampleReviewPolicyResponse> getTrainingSampleReviewPoliciesByProductLine(Long productLineId);

    TrainingSampleReviewPolicyResponse createNewReviewPolicy(TrainingSampleReviewPolicyRequest request);

    List<TrainingSampleReviewResponse> findByConfigId(Long configId);

    List<TrainingSampleReviewResponse> findByProductLine(Long productLineId);

    void deletePolicy(Long policyId);

    TrainingSampleReviewResponse assignTeamLeadToReview(TrainingSampleReviewRequest request);

    TrainingSampleReviewResponse submit(TrainingSampleReviewRequest reviewRequest, User currentUser, HttpServletRequest request);

    void approve(Long id, User currentUser, ApproveRequest approveRequest, HttpServletRequest request);

    void revise(Long id, User currentUser, HttpServletRequest request);

    void reject(Long id, User currentUser, RejectRequest rejectRequest, HttpServletRequest request);

    List<TrainingSampleReviewResponse> findByReviewedById(Long productLineId, Long reviewedId);
}
