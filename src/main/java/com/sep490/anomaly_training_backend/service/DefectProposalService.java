package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;

import java.util.List;

public interface DefectProposalService {
    List<DefectProposalResponse> getDefectProposalByTeamLeadAndGroup(Long id, String username);

    void createDefectProposal(CreateDefectProposalRequest reportRequest);
}
