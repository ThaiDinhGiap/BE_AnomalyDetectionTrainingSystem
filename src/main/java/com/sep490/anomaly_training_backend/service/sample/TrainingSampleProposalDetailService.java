package com.sep490.anomaly_training_backend.service.sample;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;

import java.util.List;

public interface TrainingSampleProposalDetailService {
    List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId);
}
