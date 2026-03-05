package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.Approvable;

public interface ApprovalHandler {

    ApprovalEntityType getType();

    void applyApproval(Approvable entity);
}
