package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.sample.CategorySample;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TrainingSampleService {
    List<TrainingSampleResponse> getTrainingSampleByProductLine(Long productLineId);

    TrainingSampleResponse getTrainingSampleById(Long id);

    List<TrainingSampleResponse> getTrainingSampleByProcess(Long id);

    List<TrainingSampleResponse> getTrainingSampleByCategory(Long id);

    List<TrainingSampleResponse> importTrainingSample(User currentUser, MultipartFile file) throws BadRequestException;

    CategorySample getCategory(Long productLineId);

    List<TrainingSampleProposalResponse> getTrainingSampleProposalByProductLine(Long id, String username);

    TrainingSampleProposalResponse createTrainingSampleProposal(TrainingSampleProposalRequest request, User currentUser);

    void deleteTrainingSampleProposal(Long id);

    TrainingSampleProposalUpdateResponse updateTrainingSampleProposal(Long id, TrainingSampleProposalRequest trainingSampleProposalRequest, User user) throws BadRequestException;

    void revise(Long id, User currentUser, HttpServletRequest request);

    // Relate approval methods
    void submit(Long proposalId, User currentUser, HttpServletRequest request);

    void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request);

    ResponseEntity<Boolean> canApprove(Long proposalId, User currentUser);

    void submitTrainingSampleProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request);

    List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingSampleProposalId);
}
