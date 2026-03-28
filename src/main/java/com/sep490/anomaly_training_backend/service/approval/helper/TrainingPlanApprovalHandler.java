package com.sep490.anomaly_training_backend.service.approval.helper;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;

public class TrainingPlanApprovalHandler implements ApprovalHandler {

    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.TRAINING_PLAN;
    }

    @Override
    public void applyApproval(Approvable entity) {
        // For training plan, we simply set the status to APPROVED
        entity.setStatus(ReportStatus.COMPLETED);
    }
}
