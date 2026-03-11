package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface TrainingSampleProposalService {
    List<TrainingSampleProposalResponse> getTrainingSampleProposalsByTeamLeadAndProductLine(Long id, String username);

    void createTrainingSampleProposal(TrainingSampleProposalRequest request);

    void deleteTrainingSampleProposal(Long id);

    TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalRequest trainingSampleProposalRequest) throws BadRequestException;

    void revise(Long id, User currentUser, HttpServletRequest request);

    // Relate approval methods
    void submit(Long proposalId, User currentUser, HttpServletRequest request);

    void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request);

    boolean canApprove(Long proposalId, User currentUser);

    void submitTrainingSampleProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request);
}
