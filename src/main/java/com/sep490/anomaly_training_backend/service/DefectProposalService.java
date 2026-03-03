package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;

import java.util.List;

public interface DefectProposalService {
    List<DefectProposalResponse> getDefectProposalByTeamLeadAndProductLine(Long id, String username);

    void createDefectProposalDraft(CreateDefectProposalRequest reportRequest);
}
