package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewPolicyRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewPolicyResponse;
import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewPolicyService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/review-training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample Review Management", description = "API for scheduling the annual review of the training sample list and reporting the results")
public class TrainingSampleReviewController {
    private final TrainingSampleReviewPolicyService trainingSampleReviewPolicyService;

    @GetMapping("/policies/product-line/{productLineId}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Get review policies by product line", description = "Retrieve a list of review policies by product line")
    public ResponseEntity<ApiResponse<List<TrainingSampleReviewPolicyResponse>>> getReviewPoliciesByProductLine(
            @PathVariable Long productLineId) {
        List<TrainingSampleReviewPolicyResponse> policies = trainingSampleReviewPolicyService
                .getTrainingSampleReviewPoliciesByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chính sách rà soát thành công", policies));
    }


    @PostMapping("/policies")
    @PreAuthorize("hasAuthority('training_sample_review.create')")
    @Operation(summary = "Create new review policy", description = "Create a new review policy for the training sample list")
    public ResponseEntity<ApiResponse<TrainingSampleReviewPolicyResponse>> createNewReviewPolicy(
            @RequestBody TrainingSampleReviewPolicyRequest request) {
        TrainingSampleReviewPolicyResponse response = trainingSampleReviewPolicyService.createNewReviewPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chính sách rà soát thành công", response));
    }


    @GetMapping("/reviews/config/{configId}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Get reviews by config", description = "Retrieve a list of training sample reviews by configuration")
    public ResponseEntity<ApiResponse<List<TrainingSampleReviewResponse>>> getReviewsByConfigId(
            @PathVariable Long configId) {
        List<TrainingSampleReviewResponse> reviews = trainingSampleReviewPolicyService.findByConfigId(configId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách rà soát thành công", reviews));
    }


    @GetMapping("/reviews/product-line/{productLineId}")
    @PreAuthorize("hasAuthority('training_sample_review.view')")
    @Operation(summary = "Get reviews by product line", description = "Lấy danh sách rà soát mẫu huấn luyện theo dây chuyền sản xuất")
    public ResponseEntity<ApiResponse<List<TrainingSampleReviewResponse>>> getReviewsByProductLine(
            @PathVariable Long productLineId) {
        List<TrainingSampleReviewResponse> reviews = trainingSampleReviewPolicyService.findByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách rà soát thành công", reviews));
    }


    @DeleteMapping("/policies/{policyId}")
    @PreAuthorize("hasAuthority('training_sample_review.delete')")
    @Operation(summary = "Delete review policy", description = "Delete review policy (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteReviewPolicy(
            @PathVariable Long policyId) {
        trainingSampleReviewPolicyService.deletePolicy(policyId);
        return ResponseEntity.ok(ApiResponse.success("Xóa chính sách rà soát thành công", null));
    }


    @PutMapping("/reviews/{reviewId}/assign-team-lead")
    @PreAuthorize("hasAuthority('training_sample_review.update')")
    @Operation(summary = "Assign team lead to review", description = "Assign a team lead to perform the training sample review")
    public ResponseEntity<ApiResponse<TrainingSampleReviewResponse>> assignTeamLeadToReview(
            @PathVariable Long reviewId,
            @RequestBody TrainingSampleReviewRequest request) {
        request.setId(reviewId);
        TrainingSampleReviewResponse response = trainingSampleReviewPolicyService.assignTeamLeadToReview(request);
        return ResponseEntity.ok(ApiResponse.success("Gán trưởng nhóm thành công", response));
    }


    @PutMapping("/reviews/{reviewId}/confirm")
    @PreAuthorize("hasAuthority('training_sample_review.update')")
    @Operation(summary = "Confirm review by team lead", description = "Confirm training sample review results")
    public ResponseEntity<ApiResponse<TrainingSampleReviewResponse>> confirmReviewByTeamLead(
            @PathVariable Long reviewId,
            @RequestBody TrainingSampleReviewRequest request) {
        request.setId(reviewId);
        TrainingSampleReviewResponse response = trainingSampleReviewPolicyService.confirmReviewByTeamLead(request);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận kết quả rà soát thành công", response));
    }

    @Operation(summary = "Revising approval (Move to Draft)", description = "Move the proposal from the pending approval status back to the Draft status for further editing.")
    @PutMapping("/{id}/revise")
    @PreAuthorize("hasAuthority('defect.revise')")
    public ResponseEntity<String> revise(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        trainingSampleReviewPolicyService.revise(id, currentUser, request);
        return ResponseEntity.ok("The proposal has been successfully moved back to the Draft status!");
    }

    @Operation(summary = "Approve defect proposal", description = "Approve the defect proposal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No approval permission"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defect Proposal is not found")
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('defect_proposal.approve')")
    public ResponseEntity<String> approveProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApproveRequest approveRequest,
            HttpServletRequest request) {

        trainingSampleReviewPolicyService.approve(id, currentUser, approveRequest, request);
        return ResponseEntity.ok("Defect Proposal has been approved successfully!");
    }

    @Operation(summary = "Reject Training Sample Review", description = "Reject and request revision of the defect proposal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Training Sample Proposal rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid rejection reason")
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('defect_proposal.edit')")
    public ResponseEntity<String> rejectProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RejectRequest rejectRequest,
            HttpServletRequest request) {

        trainingSampleReviewPolicyService.reject(id, currentUser, rejectRequest, request);
        return ResponseEntity.ok("Defect Proposal has been rejected!");
    }

}
