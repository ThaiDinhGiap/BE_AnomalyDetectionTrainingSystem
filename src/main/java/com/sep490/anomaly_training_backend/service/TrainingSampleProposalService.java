package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface TrainingSampleProposalService {
    List<TrainingSampleProposalResponse> getTrainingSampleProposalsByTeamLeadAndProductLine(Long id, String username);

    void createTrainingSampleProposal(CreateTrainingSampleProposalRequest createTrainingSampleProposalRequest);

    void deleteTrainingSampleProposal(Long id);

    TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalUpdateRequest trainingSampleProposalUpdateRequest) throws BadRequestException;

    void revise(Long id, User currentUser, HttpServletRequest request);

}
