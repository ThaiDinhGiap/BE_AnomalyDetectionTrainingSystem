package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.service.ActionItemsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/action-items")
@RequiredArgsConstructor
@Tag(name = "Action Items", description = "Endpoints for action items summary")
public class ActionItemsController {

    private final ActionItemsService actionItemsService;

    @Operation(summary = "Get pending signatures summary (role-aware: PRO_OUT for TL, FI_OUT for FI, SV for Supervisor)")
    @GetMapping("/pending-signatures")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<PendingSignatureResponse> getPendingSignatures(
            @Parameter(description = "Filter by Line ID") @RequestParam(required = false) Long lineId) {
        return ResponseEntity.ok(actionItemsService.getPendingSignatures(lineId));
    }

    @Operation(summary = "Get reports submitted by current user that are pending approval from a superior")
    @GetMapping("/submitted-pending-approval")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<PendingSignatureResponse> getSubmittedPendingApproval(
            @Parameter(description = "Filter by Line ID") @RequestParam(required = false) Long lineId) {
        return ResponseEntity.ok(actionItemsService.getSubmittedPendingApproval(lineId));
    }

    @Operation(summary = "Get failed trainings summary")
    @GetMapping("/failed-trainings")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<FailedTrainingResponse> getFailedTrainings(
            @Parameter(description = "Filter by Line ID") @RequestParam(required = false) Long lineId) {
        return ResponseEntity.ok(actionItemsService.getFailedTrainings(lineId));
    }

    @Operation(summary = "Get certificates needing monitoring (chứng chỉ cần giám sát)")
    @GetMapping("/expiring-skills")
    @PreAuthorize("hasAuthority('training_result.view')")
    public ResponseEntity<ExpiringSkillResponse> getExpiringSkills(
            @Parameter(description = "Filter by Line ID") @RequestParam(required = false) Long lineId) {
        return ResponseEntity.ok(actionItemsService.getExpiringSkills(lineId));
    }
}
