package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.DefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;

import java.util.List;

public interface DefectProposalDetailService {
    List<DefectProposalDetailResponse> getDefectProposalDetails(Long DefectProposalId);
}
