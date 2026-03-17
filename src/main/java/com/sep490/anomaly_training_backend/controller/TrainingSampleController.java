package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleProposalDetailService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleProposalService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample Management", description = "API for managing training samples and sample proposals")
public class TrainingSampleController {

    public final TrainingSampleService trainingSampleService;
    public final TrainingSampleProposalService trainingSampleProposalService;
    public final TrainingSampleProposalDetailService trainingSampleProposalDetailService;

    @Operation(summary = "Get training samples by product line")
    @GetMapping("/")
    @PreAuthorize("hasAuthority('training_sample.view')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> getTrainingSampleByProductLine(@RequestParam("productLineId") Long productLineId) {
        List<TrainingSampleResponse> list = trainingSampleService.getTrainingSampleByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get training samples group by Category")
    @GetMapping("/category")
    @PreAuthorize("hasAuthority('training_sample.view')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> getTrainingSampleByCategory(@RequestParam("id") Long id) {
        List<TrainingSampleResponse> list = trainingSampleService.getTrainingSampleByCategory(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get training samples group by process")
    @GetMapping("/process")
    @PreAuthorize("hasAuthority('training_sample.view')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> getTrainingSampleByProcess(@RequestParam("id") Long id) {
        List<TrainingSampleResponse> list = trainingSampleService.getTrainingSampleByProcess(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get training sample proposals by productLine")
    @GetMapping("/proposal")
    @PreAuthorize("hasAuthority('training_sample_proposal.view')")
    public ResponseEntity<ApiResponse<List<TrainingSampleProposalResponse>>> getTrainingSampleProposalByProductLine(
            @RequestParam("productLineId") Long productLineId,
            @AuthenticationPrincipal User currentUser) {
        List<TrainingSampleProposalResponse> list = trainingSampleProposalService.getTrainingSampleProposalsByTeamLeadAndProductLine(productLineId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get training sample proposal details")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.view')")
    public ResponseEntity<ApiResponse<List<TrainingSampleProposalDetailResponse>>> getTrainingSampleProposalDetail(@PathVariable Long id) {
        List<TrainingSampleProposalDetailResponse> list = trainingSampleProposalDetailService.getTrainingSampleProposalDetails(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Create new training sample proposal")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('training_sample_proposal.create')")
    public ResponseEntity<ApiResponse<Void>> createTrainingSampleProposal(
            @ModelAttribute("request") TrainingSampleProposalRequest request,
            @AuthenticationPrincipal User currentUser) {
        trainingSampleProposalService.createTrainingSampleProposal(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.delete')")
    public ResponseEntity<Void> deleteTrainingSampleProposal(@PathVariable("id") Long id) {
        trainingSampleProposalService.deleteTrainingSampleProposal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.edit')")
    public ResponseEntity<TrainingSampleProposalUpdateResponse> updateTrainingProposal(
            @Parameter(description = "ID of the training sample proposal that needs to be corrected") @PathVariable Long id,
            @Valid @ModelAttribute TrainingSampleProposalRequest request) throws BadRequestException {
        TrainingSampleProposalUpdateResponse response = trainingSampleProposalService.updateTrainingSampleProposal(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Approve Training Sample proposal", description = "Approve the Training Sample proposal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No approval permission"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Training Sample Proposal is not found")
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('training_sample_proposal.approve')")
    public ResponseEntity<String> approveProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApproveRequest approveRequest,
            HttpServletRequest request) {

        trainingSampleProposalService.approve(id, currentUser, approveRequest, request);
        return ResponseEntity.ok("Training Sample Proposal has been approved successfully!");
    }

    @Operation(summary = "Reject Training Sample proposal", description = "Reject and request revision of the Training Sample proposal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Training Sample Proposal rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid rejection reason")
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('training_sample_proposal.edit')")
    public ResponseEntity<String> rejectProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RejectRequest rejectRequest,
            HttpServletRequest request) {

        trainingSampleProposalService.reject(id, currentUser, rejectRequest, request);
        return ResponseEntity.ok("Training Sample Proposal has been rejected!");
    }

    @Operation(summary = "Get training sample detail information")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('defect.detail')")
    public ResponseEntity<ApiResponse<TrainingSampleResponse>> getTrainingSampleDetail(@PathVariable("id") Long id) {
        TrainingSampleResponse response = trainingSampleService.getTrainingSampleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Import data (Training Sample Banking)")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('training_sample.import')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> importTrainingSample(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        List<TrainingSampleResponse> data = trainingSampleService.importTrainingSample(currentUser, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Submit training sample proposal for approval", description = "Change training sample proposal  status from DRAFT to SUBMITTED.")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('training_sample_proposal.edit')")
    public ResponseEntity<String> submit(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request) {
        trainingSampleProposalService.submitTrainingSampleProposalForApproval(id, currentUser, request);
        return ResponseEntity.ok("Training sample proposal submitted for approval successfully!");
    }
}
