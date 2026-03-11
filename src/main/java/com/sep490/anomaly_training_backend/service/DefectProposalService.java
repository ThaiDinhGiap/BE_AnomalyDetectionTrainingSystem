package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface DefectProposalService {
    List<DefectProposalResponse> getDefectProposalByTeamLeadAndProductLine(Long id, String username);

    void createDefectProposalDraft(DefectProposalRequest reportRequest);

    void deleteDefectProposal(Long id);

    DefectProposalUpdateResponse updateDefectProposal(Long id, DefectProposalRequest request) throws BadRequestException;

    void submitDefectProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request);

    // Relate approval methods
    void submit(Long proposalId, User currentUser, HttpServletRequest request);

    void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request);

    boolean canApprove(Long proposalId, User currentUser);

    void revise(Long id, User currentUser, HttpServletRequest request);
}
