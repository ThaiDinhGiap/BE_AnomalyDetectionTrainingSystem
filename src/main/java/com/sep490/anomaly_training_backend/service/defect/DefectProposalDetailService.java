package com.sep490.anomaly_training_backend.service.defect;

import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalDetailResponse;

import java.util.List;

public interface DefectProposalDetailService {
    List<DefectProposalDetailResponse> getDefectProposalDetails(Long DefectProposalId);
}
