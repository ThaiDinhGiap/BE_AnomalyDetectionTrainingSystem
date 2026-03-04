package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface TrainingSampleReviewService {
    List<TrainingSampleReviewResponse> getTrainingSampleReviewByProductLine(Long productLineId);

    TrainingSampleReviewResponse confirmReview(User reviewedBy, TrainingSampleReviewRequest request);
}
