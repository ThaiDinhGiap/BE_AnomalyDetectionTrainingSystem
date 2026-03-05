package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.repository.DefectRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefectProposalApprovalHandler implements ApprovalHandler {

    private final DefectRepository defectRepository;

    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.DEFECT_PROPOSAL;
    }

    @Override
    public void applyApproval(Approvable entity) {

        DefectProposal proposal = (DefectProposal) entity;

    }
}
