package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.dto.response.RejectReasonGroupResponse;
import com.sep490.anomaly_training_backend.dto.response.RequiredActionResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface ApprovalQueryService {

    List<ApprovalHistoryResponse> getApprovalHistory(ApprovalEntityType entityType, Long entityId);

    List<ApprovalHistoryResponse> getApprovalHistoryByVersion(ApprovalEntityType entityType, Long entityId, Integer version);

    List<PendingApprovalResponse> getPendingApprovals(User currentUser, ApprovalEntityType entityType);

    Long countPendingApprovals(User currentUser);

    List<ApprovalHistoryResponse> getActionsByUser(Long userId, int page, int size);

    // ── Timeline ──

    ApprovalTimelineResponse getTimeline(ApprovalEntityType entityType, Long entityId);

    // ── Metadata ──

    List<RejectReasonGroupResponse> getRejectReasonGroups();

    List<RequiredActionResponse> getRequiredActions();
}