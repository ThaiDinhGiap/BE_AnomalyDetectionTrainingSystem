package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.DefectProposalService;
import com.sep490.anomaly_training_backend.service.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defect Management", description = "API quản lý lỗi quá khứ và đề xuất ghi nhận lỗi")
public class DefectController {
    private final DefectService defectService;
    private final DefectProposalService defectProposalService;
    private final DefectProposalDetailService defectProposalDetailService;

    @Operation(summary = "Get defects by productLine (Defect Banking)")
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> getDefectByProductLine(@RequestParam("productLineId")Long productLineId) {
        List<DefectResponse> list = defectService.getDefectByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get defect proposals by productLine")
    @GetMapping("/proposal")
    @PreAuthorize("hasAnyAuthority('defect.view', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPERVISOR', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<DefectProposalResponse>>> getDefectProposalByGroup(
            @RequestParam("productLineId")Long productLineId,
            @AuthenticationPrincipal User currentUser) {

            List<DefectProposalResponse> list = defectProposalService.getDefectProposalByTeamLeadAndProductLine(productLineId, currentUser.getUsername());
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
        defectProposalService.createDefectProposalDraft(createDefectProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('defect.delete', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<Void> deleteDefectProposal(@PathVariable("id") Long id){
        defectProposalService.deleteDefectProposal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('defect.edit', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<DefectProposalUpdateResponse> updateDefectProposal(
            @Parameter(description = "ID của đề xuất mẫu lỗi quá khứ cần sửa") @PathVariable Long id,
            @Valid @RequestBody DefectProposalUpdateRequest request) throws BadRequestException {
        DefectProposalUpdateResponse response = defectProposalService.updateDefectProposal(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Hoàn duyệt (Chuyển về Nháp)", description = "Chuyển đề xất từ trạng thái chờ duyệt về lại trạng thái Nháp để chỉnh sửa.")
    @PutMapping("/{id}/revise")
    @PreAuthorize("hasAnyAuthority('defect.revise', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<String> revise(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        defectProposalService.revise(id, currentUser, request);
        return ResponseEntity.ok("Đã chuyển kế hoạch về trạng thái nháp thành công!");
    }

}
