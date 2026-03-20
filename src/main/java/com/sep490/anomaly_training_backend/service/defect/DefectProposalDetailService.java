package com.sep490.anomaly_training_backend.service.defect;

import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface DefectProposalDetailService {
    List<DefectProposalDetailResponse> getDefectProposalDetails(Long DefectProposalId);

    void saveFeedback(Long detailId, DetailFeedbackRequest request, User currentUser);

    void clearFeedback(Long proposalId);
}
