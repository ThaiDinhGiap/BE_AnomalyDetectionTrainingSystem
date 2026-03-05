package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingSampleProposalUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalDetailService;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalService;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample Management", description = "API for managing training samples and sample proposals")
public class TrainingSampleController {

    public final TrainingSampleService trainingSampleService;
    public final TrainingSampleProposalService trainingSampleProposalService;
    public final TrainingSampleProposalDetailService trainingSampleProposalDetailService;

    @Operation(summary = "Get training samples by group")
    @GetMapping("/")
    @PreAuthorize("hasAuthority('training_sample.view')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> getTrainingSampleByProductLine(@RequestParam("productLineId") Long productLineId) {
        List<TrainingSampleResponse> list = trainingSampleService.getTrainingSampleByProductLine(productLineId);
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
    public ResponseEntity<ApiResponse<Void>> createTrainingSampleProposal(@RequestBody CreateTrainingSampleProposalRequest createTrainingSampleProposalRequest) {
        trainingSampleProposalService.createTrainingSampleProposal(createTrainingSampleProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.delete')")
    public ResponseEntity<Void> deleteTrainingSampleProposal(@PathVariable("id") Long id){
        trainingSampleProposalService.deleteTrainingSampleProposal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('training_sample_proposal.edit')")
    public ResponseEntity<TrainingSampleProposalUpdateResponse> updateTrainingProposal(
            @Parameter(description = "ID of the training sample proposal that needs to be corrected") @PathVariable Long id,
            @Valid @RequestBody TrainingSampleProposalUpdateRequest request) throws BadRequestException {
        TrainingSampleProposalUpdateResponse response = trainingSampleProposalService.updateTrainingSampleProposal(id, request);
        return ResponseEntity.ok(response);
    }
}
