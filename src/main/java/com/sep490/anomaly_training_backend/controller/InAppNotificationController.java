package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.InAppNotificationDto;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "In-App Notifications")
public class InAppNotificationController {

    private final InAppNotificationService notifService;

    @Operation(summary = "Get unread list (bell dropdown, max 20)")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InAppNotificationDto>>> getUnread(
            @AuthenticationPrincipal User me) {

        return ResponseEntity.ok(ApiResponse.success(
                notifService.getUnread(me.getId())
        ));
    }

    @Operation(summary = "Number unread notifications (badge)")
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> countUnread(
            @AuthenticationPrincipal User me) {

        return ResponseEntity.ok(ApiResponse.success(
                notifService.countUnread(me.getId())
        ));
    }

    @Operation(summary = "All notifications, has pageable")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InAppNotificationDto>>> getAll(
            @AuthenticationPrincipal User me,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                notifService.getAll(me.getId(), pageable)
        ));
    }

    @Operation(summary = "Mark a notification as read")
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notifService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "Mark all notifications as read")
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User me) {

        notifService.markAllAsRead(me.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "Delete a notification")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User me) {

        notifService.delete(id, me.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
