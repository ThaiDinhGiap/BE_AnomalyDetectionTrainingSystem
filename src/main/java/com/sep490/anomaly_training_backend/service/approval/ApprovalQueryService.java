package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.dto.response.RejectReasonGroupResponse;
import com.sep490.anomaly_training_backend.dto.response.RequiredActionResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface ApprovalQueryService {

    List<PendingApprovalResponse> getPendingApprovals(User currentUser, ApprovalEntityType entityType);

    Long countPendingApprovals(User currentUser);

    List<ApprovalHistoryResponse> getActionsByUser(Long userId, int page, int size);

    // ── Metadata (merged from ApprovalMetadataService) ──

    List<RejectReasonGroupResponse> getRejectReasonGroups();

    List<RequiredActionResponse> getRequiredActions();
}