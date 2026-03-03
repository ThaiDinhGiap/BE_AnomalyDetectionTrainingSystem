package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import java.util.List;

public interface TrainingSampleProposalService {

    List<TrainingSampleProposalResponse> getTrainingSampleProposalsByTeamLeadAndGroup(Long id, String username);

    void createTrainingSampleProposal(CreateTrainingSampleProposalRequest createTrainingSampleProposalRequest);

}
