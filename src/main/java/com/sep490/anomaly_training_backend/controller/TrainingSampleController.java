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
@Tag(name = "Training Sample Management", description = "API quản lý mẫu huấn luyện và đề xuất xây dựng mẫu")
public class TrainingSampleController {

    public final TrainingSampleService trainingSampleService;
    public final TrainingSampleProposalService trainingSampleProposalService;
    public final TrainingSampleProposalDetailService trainingSampleProposalDetailService;

    @Operation(summary = "Lấy danh sách mẫu huấn luyện theo nhóm")
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('training_sample.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TrainingSampleResponse>>> getTrainingSampleByProductLine(@RequestParam("productLineId") Long productLineId) {
        List<TrainingSampleResponse> list = trainingSampleService.getTrainingSampleByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Lấy danh sách đề xuất mẫu huấn luyện theo dây truyền")
    @GetMapping("/proposal")
    @PreAuthorize("hasAnyAuthority('training_sample.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TrainingSampleProposalResponse>>> getTrainingSampleReportByProductLine(
            @RequestParam("productLineId") Long productLineId,
            @AuthenticationPrincipal User currentUser) {
        List<TrainingSampleProposalResponse> list = trainingSampleProposalService.getTrainingSampleProposalsByTeamLeadAndProductLine(productLineId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Lấy chi tiết đề xuất mẫu huấn luyện")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyAuthority('training_sample.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<TrainingSampleProposalDetailResponse>>> getTrainingSampleDetail(@PathVariable Long id) {
        List<TrainingSampleProposalDetailResponse> list = trainingSampleProposalDetailService.getTrainingSampleProposalDetails(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Tạo đề xuất mẫu huấn luyện mới")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('training_sample.create', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<Void>> createDefectProposal(@RequestBody CreateTrainingSampleProposalRequest createTrainingSampleProposalRequest) {
        trainingSampleProposalService.createTrainingSampleProposal(createTrainingSampleProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTrainingSampleProposal(@PathVariable("id") Long id){
        trainingSampleProposalService.deleteTrainingSampleProposal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('training_sample.edit', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TrainingSampleProposalUpdateResponse> updateTrainingPlan(
            @Parameter(description = "ID của đề xuất mẫu huấn luyện cần sửa") @PathVariable Long id,
            @Valid @RequestBody TrainingSampleProposalUpdateRequest request) throws BadRequestException {
        TrainingSampleProposalUpdateResponse response = trainingSampleProposalService.updateTrainingSampleProposal(id, request);
        return ResponseEntity.ok(response);
    }
}
