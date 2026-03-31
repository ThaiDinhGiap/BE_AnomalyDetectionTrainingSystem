package com.sep490.anomaly_training_backend.service.sample;

import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalDetailResponse;

import java.util.List;

public interface TrainingSampleProposalDetailService {
    List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId);
}
