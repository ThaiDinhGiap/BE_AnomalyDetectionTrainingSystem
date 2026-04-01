package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectCoverageResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectInProcess;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DefectService {
//    List<DefectResponse> getDefectBySupervisor(Long userId);

    List<DefectResponse> getDefectByProductLine(Long productLineId);

    List<DefectResponse> getDefectByProcess(Long processId);

    DefectResponse getDefectById(Long id);

    Boolean checkExistDefectDescription(String defectDescription);

    List<DefectResponse> importDefect(User currentUser, MultipartFile file) throws BadRequestException;

    DefectCoverageResponse getCoverageInProductLine(Long productLineId);

    List<DefectInProcess> countDefectInProcess(Long productLineId);

    List<DefectProposalResponse> getDefectProposalByProductLine(Long id, String username);

    DefectProposalResponse createDefectProposalDraft(DefectProposalRequest reportRequest, User currentUser);

    void deleteDefectProposal(Long id);

    DefectProposalUpdateResponse updateDefectProposal(Long id, DefectProposalRequest request, User user) throws BadRequestException;

    void submitDefectProposalForApproval(Long proposalId, User currentUser, HttpServletRequest request);

    // Relate approval methods
    void submit(Long proposalId, User currentUser, HttpServletRequest request);

    void approve(Long proposalId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long proposalId, User currentUser, RejectRequest req, HttpServletRequest request);

    ResponseEntity<Boolean> canApprove(Long proposalId, User currentUser);

    void revise(Long id, User currentUser, HttpServletRequest request);

    List<DefectProposalDetailResponse> getDefectProposalDetails(Long defectProposalId);

}
