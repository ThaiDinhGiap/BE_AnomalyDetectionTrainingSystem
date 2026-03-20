package com.sep490.anomaly_training_backend.service.sample;

import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface TrainingSampleProposalDetailService {
    List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId);

    void saveFeedback(Long detailId, DetailFeedbackRequest request, User currentUser);
}
