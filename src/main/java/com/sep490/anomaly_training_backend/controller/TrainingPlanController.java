package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-plans")
@RequiredArgsConstructor
@Tag(name = "Training Plan Management", description = "API for creating, updating and approving training plans")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @Operation(summary = "Create new training plan", description = "Create a training plan in DRAFT status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('training_plan.create')")
    public ResponseEntity<TrainingPlanResponse> createPlan(@Valid @RequestBody TrainingPlanCreateRequest request) {
        TrainingPlanResponse response = trainingPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get training plan details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<TrainingPlanResponse> getPlanDetail(
            @Parameter(description = "Plan ID") @PathVariable Long id) {
        return ResponseEntity.ok(trainingPlanService.getPlanDetail(id));
    }

    @Operation(summary = "Get all training plans")
    @GetMapping
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<List<TrainingPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(trainingPlanService.getAllPlans());
    }

    @Operation(summary = "Get groups (Lines) managed by current user")
    @GetMapping("/my-managed-groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(trainingPlanService.getMyManagedGroups());
    }

    @Operation(
            summary = "Update training plan content",
            description = "Update employee list, processes and planned dates. " +
                    "NOTE: If rescheduling, past dates without Actual Date will be automatically marked as 'Absent'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "400", description = "Schedule update logic error")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('training_plan.edit')")
    public ResponseEntity<TrainingPlanResponse> updateTrainingPlan(
            @Parameter(description = "Plan ID to update") @PathVariable Long id,
            @Valid @RequestBody TrainingPlanUpdateRequest request) {

        TrainingPlanResponse response = trainingPlanService.updatePlan(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Submit plan for approval", description = "Change plan status from DRAFT to SUBMITTED.")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('training_plan.submit')")
    public ResponseEntity<String> submit(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request) {
        trainingPlanService.submitPlanForApproval(id, currentUser, request);
        return ResponseEntity.ok("Plan submitted for approval successfully!");
    }

    @Operation(summary = "Revise plan (Return to Draft)", description = "Move plan from pending approval back to Draft status for editing.")
    @PutMapping("/{id}/revise")
    @PreAuthorize("hasAuthority('training_plan.revise')")
    public ResponseEntity<String> revise(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        trainingPlanService.revise(id, currentUser, request);
        return ResponseEntity.ok("Plan has been moved back to draft status successfully!");
    }

    @Operation(summary = "Check user approval permission for plan")
    @GetMapping("/{id}/permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> getApprovePermission(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Plan ID") @PathVariable Long id) {
        return ResponseEntity.ok(trainingPlanService.canApprove(id, currentUser));
    }

    @Operation(summary = "Approve training plan", description = "Approve the training plan. Only authorized personnel can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approved successfully"),
            @ApiResponse(responseCode = "403", description = "No approval permission"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('training_plan.approve')")
    public ResponseEntity<String> approvePlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApproveRequest approveRequest,
            HttpServletRequest request) {

        trainingPlanService.approve(id, currentUser, approveRequest, request);
        return ResponseEntity.ok("Plan has been approved successfully!");
    }

    @Operation(summary = "Reject training plan", description = "Reject and request revision of the plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid rejection reason")
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('training_plan.reject')")
    public ResponseEntity<String> rejectPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RejectRequest rejectRequest,
            HttpServletRequest request) {

        trainingPlanService.reject(id, currentUser, rejectRequest, request);
        return ResponseEntity.ok("Plan has been rejected!");
    }
}