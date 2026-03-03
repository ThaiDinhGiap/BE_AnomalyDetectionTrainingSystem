package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.DefectProposalService;
import com.sep490.anomaly_training_backend.service.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defect Management", description = "API quản lý lỗi quá khứ và đề xuất ghi nhận lỗi")
public class DefectController {
    private final DefectService defectService;
    private final DefectProposalService defectProposalService;
    private final DefectProposalDetailService defectProposalDetailService;

    @Operation(summary = "Lấy danh sách lỗi theo nhóm (Defect Banking)")
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> getDefectByGroup(@RequestParam("productLineId")Long productLineId) {
        List<DefectResponse> list = defectService.getDefectByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Lấy danh sách đề xuất ghi nhận lỗi theo nhóm")
    @GetMapping("/proposal")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectProposalResponse>>> getDefectProposalByGroup(
            @RequestParam("productLineId")Long productLineId,
            @AuthenticationPrincipal User currentUser) {
        List<DefectProposalResponse> list = defectProposalService.getDefectProposalByTeamLeadAndProductLine(productLineId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Lấy chi tiết đề xuất ghi nhận lỗi")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectProposalDetailResponse>>> getDefectProposalDetail(@PathVariable Long id) {
        List<DefectProposalDetailResponse> list = defectProposalDetailService.getDefectProposalDetails(id);        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Tạo đề xuất ghi nhận lỗi mới")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('defect.create', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<Void>> createDefectProposal(@RequestBody CreateDefectProposalRequest createDefectProposalRequest) {
        defectProposalService.createDefectProposalDraft(createDefectProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }


}
