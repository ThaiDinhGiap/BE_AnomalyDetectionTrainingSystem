package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse;
import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.dto.response.RejectReasonGroupResponse;
import com.sep490.anomaly_training_backend.dto.response.RequiredActionResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.approval.ApprovalQueryService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval Management", description = "Manage approval workflow for reports and plans")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalQueryService approvalQueryService;
    private final ApprovalTimelineService approvalTimelineService;

    // ==================== PENDING LIST ====================

    @GetMapping("/pending")
    @Operation(summary = "Get pending approvals for current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PendingApprovalResponse>>> getPendingApprovals(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) ApprovalEntityType entityType) {

        List<PendingApprovalResponse> pending = approvalQueryService.getPendingApprovals(currentUser, entityType);

        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @GetMapping("/pending/count")
    @Operation(summary = "Count pending approvals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getPendingCount(
            @AuthenticationPrincipal User currentUser) {

        Long count = approvalQueryService.countPendingApprovals(currentUser);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ==================== HISTORY ====================

    @GetMapping("/history/{entityType}/{entityId}")
    @Operation(summary = "Get approval history for a report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ApprovalHistoryResponse>>> getApprovalHistory(
            @PathVariable ApprovalEntityType entityType,
            @PathVariable Long entityId,
            @RequestParam(required = false) Integer version) {

        List<ApprovalActionLog> logs;
        if (version != null) {
            logs = approvalService.getApprovalHistoryByVersion(entityType, entityId, version);
        } else {
            logs = approvalService.getApprovalHistory(entityType, entityId);
        }

        List<ApprovalHistoryResponse> response = logs.stream()
                .map(this::toHistoryResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-actions")
    @Operation(summary = "Get action history of current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ApprovalHistoryResponse>>> getMyActions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ApprovalHistoryResponse> response = approvalQueryService.getActionsByUser(currentUser.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/metadata/reject-reasons")
    @Operation(summary = "Get grouped reject reasons for rejection form")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RejectReasonGroupResponse>>> getRejectReasons() {
        List<RejectReasonGroupResponse> reasons = approvalQueryService.getRejectReasonGroups();
        return ResponseEntity.ok(ApiResponse.success(reasons));
    }

    @GetMapping("/metadata/required-actions")
    @Operation(summary = "Get list of required actions when rejecting")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RequiredActionResponse>>> getRequiredActions() {
        List<RequiredActionResponse> actions = approvalQueryService.getRequiredActions();
        return ResponseEntity.ok(ApiResponse.success(actions));
    }

    @Operation(summary = "Get approval timeline for a report")
    @GetMapping("/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApprovalTimelineResponse> getTimeline(
            @PathVariable ApprovalEntityType entityType,
            @PathVariable Long entityId) {

        return ResponseEntity.ok(approvalTimelineService.getTimeline(entityType, entityId));
    }

    @Operation(summary = "Save feedback reject for 1 detail")
    @PutMapping("/details/{detailId}/{entityType}/feedback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> saveDetailFeedback(
            @PathVariable ApprovalEntityType entityType,
            @PathVariable Long detailId,
            @RequestBody DetailFeedbackRequest request,
            @AuthenticationPrincipal User currentUser) {

        approvalService.saveFeedback(entityType, detailId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== HELPER ====================

    private ApprovalHistoryResponse toHistoryResponse(ApprovalActionLog log) {
        Set<ApprovalHistoryResponse.RejectReasonResponse> rejectReasons =
                log.getRejectReasons() == null ? Set.of() :
                        log.getRejectReasons().stream()
                                .map(r -> ApprovalHistoryResponse.RejectReasonResponse.builder()
                                        .id(r.getId())
                                        .categoryName(r.getCategoryName())
                                        .reasonName(r.getReasonName())
                                        .build())
                                .collect(Collectors.toSet());

        Set<ApprovalHistoryResponse.RequiredActionResponse> requiredActions =
                log.getRequiredActions() == null ? Set.of() :
                        log.getRequiredActions().stream()
                                .map(a -> ApprovalHistoryResponse.RequiredActionResponse.builder()
                                        .id(a.getId())
                                        .actionName(a.getActionName())
                                        .build())
                                .collect(Collectors.toSet());

        return ApprovalHistoryResponse.builder()
                .id(log.getId())
                .entityVersion(log.getEntityVersion())
                .stepOrder(log.getStepOrder())
                .stepName(getStepName(log.getStepOrder()))
                .requiredPermission(log.getRequiredPermission())
                .action(log.getAction())
                .performedByUsername(log.getPerformedByUsername())
                .performedByFullName(log.getPerformedByFullName())
                .performedByRole(log.getPerformedByRole())
                .comment(log.getComment())
                .rejectReasons(rejectReasons)
                .requiredActions(requiredActions)
                .performedAt(log.getPerformedAt())
                .ipAddress(log.getIpAddress())
                .build();
    }

    private String getStepName(int stepOrder) {
        return switch (stepOrder) {
            case -1 -> "Revise";
            case 0 -> "Submit";
            default -> "Step " + stepOrder;
        };
    }
}