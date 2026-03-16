package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanGenerationRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanGenerationResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation(summary = "Get rejected/revise training plans",
            description = "Returns plans with status: REVISE, REJECTED_BY_SV, REJECTED_BY_MANAGER")
    @GetMapping("/rejected")
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<List<TrainingPlanResponse>> getRejectedPlans() {
        return ResponseEntity.ok(trainingPlanService.getRejectedPlans());
    }

//    @Operation(summary = "Get groups (Lines) managed by current user")
//    @GetMapping("/my-managed-groups")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<List<GroupResponse>> getMyGroups() {
//        return ResponseEntity.ok(trainingPlanService.getMyManagedGroups());
//    }

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

    @Operation(summary = "Delete training plan", description = "Delete a training plan. Only DRAFT or REJECTED plans can be deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete approved/submitted plan")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('training_plan.delete')")
    public ResponseEntity<String> deletePlan(
            @Parameter(description = "Plan ID to delete") @PathVariable Long id) {
        trainingPlanService.deletePlan(id);
        return ResponseEntity.ok("Training plan deleted successfully!");
    }

    @Operation(summary = "Delete training plan detail", description = "Delete a specific detail row from the training plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detail deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Detail not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete detail from approved/submitted plan")
    })
    @DeleteMapping("/{planId}/details/{detailId}")
    @PreAuthorize("hasAuthority('training_plan.edit')")
    public ResponseEntity<String> deleteDetail(
            @Parameter(description = "Plan ID") @PathVariable Long planId,
            @Parameter(description = "Detail ID to delete") @PathVariable Long detailId) {
        trainingPlanService.deleteDetail(planId, detailId);
        return ResponseEntity.ok("Training plan detail deleted successfully!");
    }

    // ==================== DETAIL MANAGEMENT ====================

    @Operation(summary = "Add detail to training plan", description = "Add a new detail row (employee + process + schedule) to an existing training plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Detail added successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "400", description = "Invalid detail data or plan is in pending approval status")
    })
    @PostMapping("/{planId}/details")
    @PreAuthorize("hasAuthority('training_plan.edit')")
    public ResponseEntity<TrainingPlanDetailResponse> addDetail(
            @Parameter(description = "Plan ID") @PathVariable Long planId,
            @Valid @RequestBody TrainingPlanDetailRequest request) {
        TrainingPlanDetailResponse response = trainingPlanService.addDetail(planId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update a specific detail in training plan", description = "Update employee, process, schedule or note of a detail row.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detail updated successfully"),
            @ApiResponse(responseCode = "404", description = "Plan or detail not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data or plan is in pending approval status")
    })
    @PutMapping("/{planId}/details/{detailId}")
    @PreAuthorize("hasAuthority('training_plan.edit')")
    public ResponseEntity<TrainingPlanDetailResponse> updateDetail(
            @Parameter(description = "Plan ID") @PathVariable Long planId,
            @Parameter(description = "Detail ID to update") @PathVariable Long detailId,
            @Valid @RequestBody TrainingPlanDetailRequest request) {
        TrainingPlanDetailResponse response = trainingPlanService.updateDetail(planId, detailId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get employees not yet in the plan", description = "Returns list of employees in the same group who have not been added to this plan.")
    @GetMapping("/{planId}/employees-not-in-plan")
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesNotInPlan(
            @Parameter(description = "Plan ID") @PathVariable Long planId) {
        return ResponseEntity.ok(trainingPlanService.getEmployeesNotInPlan(planId));
    }

    @Operation(summary = "Get all employees in plan's team", description = "Returns list of all active employees in the same team as the plan. Used for the training plan screen to select employees.")
    @GetMapping("/{planId}/employees")
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesInTeam(
            @Parameter(description = "Plan ID") @PathVariable Long planId) {
        return ResponseEntity.ok(trainingPlanService.getEmployeesInTeams(planId));
    }


    @Operation(summary = "Get product lines by group", description = "Returns list of product lines belonging to a specific group (dây chuyền).")
    @GetMapping("/product-lines-by-group/{groupId}")
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<List<ProductLineResponse>> getProductLinesByGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        return ResponseEntity.ok(trainingPlanService.getProductLinesByGroupId(groupId));
    }

    @Operation(summary = "Get processes by product line", description = "Returns list of processes belonging to a specific product line.")
    @GetMapping("/processes-by-line/{productLineId}")
    @PreAuthorize("hasAuthority('training_plan.view')")
    public ResponseEntity<List<ProcessResponse>> getProcessesByProductLine(
            @Parameter(description = "Product Line ID") @PathVariable Long productLineId) {
        return ResponseEntity.ok(trainingPlanService.getProcessesByProductLine(productLineId));
    }

    // ==================== APPROVAL WORKFLOW ====================

    @Operation(summary = "Submit plan for approval", description = "Change plan status from DRAFT to SUBMITTED.")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('training_plan.edit')")
    public ResponseEntity<String> submit(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request) {
        trainingPlanService.submitPlanForApproval(id, currentUser, request);
        return ResponseEntity.ok("Plan submitted for approval successfully!");
    }

    @Operation(summary = "Revise plan (Return to Draft)", description = "Move plan from pending approval back to Draft status for editing.")
    @PutMapping("/{id}/revise")
    @PreAuthorize("hasAuthority('training_plan.edit')")
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
    @PreAuthorize("hasAuthority('training_plan.edit')")
    public ResponseEntity<String> rejectPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RejectRequest rejectRequest,
            HttpServletRequest request) {

        trainingPlanService.reject(id, currentUser, rejectRequest, request);
        return ResponseEntity.ok("Plan has been rejected!");
    }

    @Operation(summary = "Generate Training Plan", description = "Automative generate training plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan generated successfully"),
            @ApiResponse(responseCode = "400", description = "Generation failed due to invalid data or system error")
    })
    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('training_plan.create', 'training_plan.edit')")
    public TrainingPlanGenerationResponse generatePlan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TrainingPlanGenerationRequest request) {
        return trainingPlanService.generateTrainingPlans(currentUser, request);
    }
}