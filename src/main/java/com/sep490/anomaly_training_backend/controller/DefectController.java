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
@Tag(name = "Defect Management", description = "API for managing past defects and defect proposals")
public class DefectController {
    private final DefectService defectService;
    private final DefectProposalService defectProposalService;
    private final DefectProposalDetailService defectProposalDetailService;

    @Operation(summary = "Get defects by group (Defect Banking)")
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> getDefectByGroup(@RequestParam("groupId") Long groupId) {
        List<DefectResponse> list = defectService.getDefectByGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get defect proposals by group")
    @GetMapping("/proposal")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectProposalResponse>>> getDefectProposalByGroup(
            @RequestParam("groupId") Long groupId,
            @AuthenticationPrincipal User currentUser) {
        List<DefectProposalResponse> list = defectProposalService.getDefectProposalByTeamLeadAndGroup(groupId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get defect proposal details")
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectProposalDetailResponse>>> getDefectProposalDetail(@PathVariable Long id) {
        List<DefectProposalDetailResponse> list = defectProposalDetailService.getDefectProposalDetails(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Create new defect proposal")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('defect.create', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<Void>> createDefectProposal(@RequestBody CreateDefectProposalRequest createDefectProposalRequest) {
        defectProposalService.createDefectProposal(createDefectProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
}
