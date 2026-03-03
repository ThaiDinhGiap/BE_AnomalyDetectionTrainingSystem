package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.approval.ApprovalQueryService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval Management", description = "Quản lý phê duyệt các báo cáo/kế hoạch")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalQueryService approvalQueryService;

    // ==================== PENDING LIST ====================

    @GetMapping("/pending")
    @Operation(summary = "Lấy danh sách báo cáo chờ phê duyệt của user hiện tại")
    @PreAuthorize("hasAnyAuthority('approval.view', 'ROLE_SUPERVISOR', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<PendingApprovalResponse>>> getPendingApprovals(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) ApprovalEntityType entityType) {

        List<PendingApprovalResponse> pending = approvalQueryService.getPendingApprovals(currentUser, entityType);

        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @GetMapping("/pending/count")
    @Operation(summary = "Đếm số báo cáo chờ phê duyệt")
    @PreAuthorize("hasAnyAuthority('approval.view', 'ROLE_SUPERVISOR', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Long>> getPendingCount(
            @AuthenticationPrincipal User currentUser) {

        Long count = approvalQueryService.countPendingApprovals(currentUser);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ==================== HISTORY ====================

    @GetMapping("/history/{entityType}/{entityId}")
    @Operation(summary = "Lấy lịch sử phê duyệt của một báo cáo")
    @PreAuthorize("hasAnyAuthority('approval.view_history', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
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
    @Operation(summary = "Lấy lịch sử các hành động của user hiện tại")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ApprovalHistoryResponse>>> getMyActions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ApprovalHistoryResponse> response = approvalQueryService.getActionsByUser(currentUser.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== HELPER ====================

    private ApprovalHistoryResponse toHistoryResponse(ApprovalActionLog log) {
        return ApprovalHistoryResponse.builder()
                .id(log.getId())
                .entityVersion(log.getEntityVersion())
                .stepOrder(log.getStepOrder())
                .stepName(getStepName(log.getStepOrder()))
                .requiredRole(log.getRequiredRole())
                .action(log.getAction())
                .performedByUsername(log.getPerformedByUsername())
                .performedByFullName(log.getPerformedByFullName())
                .performedByRole(log.getPerformedByRole())
                .comment(log.getComment())
//                .rejectReason(log.getRejectReason())
                .performedAt(log.getPerformedAt())
                .ipAddress(log.getIpAddress())
                .build();
    }

    private String getStepName(int stepOrder) {
        return switch (stepOrder) {
            case -1 -> "Revise";
            case 0 -> "Submit";
            case 1 -> "Supervisor Review";
            case 2 -> "Manager Approval";
            default -> "Step " + stepOrder;
        };
    }
}