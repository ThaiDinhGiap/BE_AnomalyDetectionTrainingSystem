package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;

/**
 * Interface for entities which need approval workflow
 */
public interface Approvable {

    Long getId();

    ApprovalEntityType getEntityType();

    ReportStatus getStatus();

    void setStatus(ReportStatus status);

    Integer getCurrentVersion();

    void setCurrentVersion(Integer version);

    Long getGroupId();

    String computeContentHash();

    /**
     * Apply when APPROVED (create/update/delete master data)
     */
    void applyApproval();
}