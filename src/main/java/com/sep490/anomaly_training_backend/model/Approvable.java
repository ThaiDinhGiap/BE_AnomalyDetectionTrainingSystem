package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
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

    void clearRejectFeedback();

    /**
     * Label hiển thị cho notification (title hoặc formCode).
     * Entity cụ thể nên override để trả tên rõ ràng hơn.
     */
    default String getEntityLabel() {
        return getEntityType().name() + "#" + getId();
    }
}