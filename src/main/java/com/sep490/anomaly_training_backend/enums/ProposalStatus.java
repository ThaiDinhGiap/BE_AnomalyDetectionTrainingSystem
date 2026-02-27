package com.sep490.anomaly_training_backend.enums;

/**
 * Proposal status for defect proposals and training sample proposals
 */
public enum ProposalStatus {
    DRAFT,
    WAITING_SV,
    REJECTED_BY_SV,
    WAITING_MANAGER,
    REJECTED_BY_MANAGER,
    APPROVED
}
