package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "API for managing notifications and templates")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/nudge/{entityType}/{entityId}")
    @Operation(
            summary = "Gửi mail nhắc phê duyệt",
            description = """
                    Gửi email nhắc người phê duyệt (SV hoặc Manager) ký duyệt tài liệu.
                    Áp dụng cho tất cả loại tài liệu: TRAINING_PLAN, TRAINING_RESULT,
                    DEFECT_PROPOSAL, TRAINING_SAMPLE_PROPOSAL.
                    Chỉ gửi được khi tài liệu đang ở trạng thái WAITING_SV hoặc WAITING_MANAGER.
                    """
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> nudgeApproval(
            @Parameter(description = "Loại tài liệu cần nhắc duyệt")
            @PathVariable ApprovalEntityType entityType,

            @Parameter(description = "ID của tài liệu")
            @PathVariable Long entityId) {

        notificationService.sendReminderNotificationManually(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
