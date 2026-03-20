package com.sep490.anomaly_training_backend.service.sample;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TrainingSampleProposalService {
    List<TrainingSampleProposalResponse> getTrainingSampleProposalByProductLine(Long id, String username);

    void createTrainingSampleProposal(TrainingSampleProposalRequest request, User currentUser);

    void deleteTrainingSampleProposal(Long id);

    TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalRequest trainingSampleProposalRequest, User user) throws BadRequestException;

    void revise(Long id, User currentUser, HttpServletRequest request);

    // Relate approval methods
    void submit(Long proposalId, User currentUser, HttpServletRequest request);

    void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request);

    ResponseEntity<Boolean> canApprove(Long proposalId, User currentUser);

    void submitTrainingSampleProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request);
}
