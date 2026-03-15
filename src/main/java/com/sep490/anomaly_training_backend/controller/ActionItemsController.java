package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.service.ActionItemsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/action-items")
@RequiredArgsConstructor
@Tag(name = "Action Items", description = "Endpoints for action items")
public class ActionItemsController {

    private final ActionItemsService actionItemsService;

    @Operation(summary = "Get pending signatures for Team Lead")
    @GetMapping("/pending-signatures")
    @PreAuthorize("hasAuthority('action_item.view')")
    public ResponseEntity<List<PendingSignatureResponse>> getPendingSignatures() {
        return ResponseEntity.ok(actionItemsService.getPendingSignatures());
    }

    @Operation(summary = "Get failed trainings for Team Lead")
    @GetMapping("/failed-trainings")
    @PreAuthorize("hasAuthority('action_item.view')")
    public ResponseEntity<List<FailedTrainingResponse>> getFailedTrainings() {
        return ResponseEntity.ok(actionItemsService.getFailedTrainings());
    }

    @Operation(summary = "Get expiring skills for Team Lead")
    @GetMapping("/expiring-skills")
    @PreAuthorize("hasAuthority('action_item.view')")
    public ResponseEntity<List<ExpiringSkillResponse>> getExpiringSkills() {
        return ResponseEntity.ok(actionItemsService.getExpiringSkills());
    }
}
