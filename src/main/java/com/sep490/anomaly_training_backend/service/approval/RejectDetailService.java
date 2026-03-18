package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.User;

public interface RejectDetailService {
    void saveFeedback(ApprovalEntityType entityType, Long detailId, DetailFeedbackRequest request, User currentUser);
}
