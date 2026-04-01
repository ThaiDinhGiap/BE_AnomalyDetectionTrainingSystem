package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.ImportHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectCoverageResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectInProcess;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.DefectService;
import com.sep490.anomaly_training_backend.service.ImportHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defect Management", description = "API quản lý lỗi quá khứ và đề xuất ghi nhận lỗi")
public class DefectController {
    private final DefectService defectService;
    private final ImportHistoryService importHistoryService;

    @Operation(summary = "Get defects by productLine (Defect Banking)")
    @GetMapping
    @PreAuthorize("hasAuthority('defect.view')")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> getDefectByProductLine(@RequestParam("productLineId") Long productLineId) {
        List<DefectResponse> list = defectService.getDefectByProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get defects by process ")
    @GetMapping("/by-process")
    @PreAuthorize("hasAuthority('defect.view')")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> getDefectByProcess(@RequestParam("processId") Long processId) {
        List<DefectResponse> list = defectService.getDefectByProcess(processId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Count defect type in product line in each process ")
    @GetMapping("/product-lines/{productLineId}/count")
    @PreAuthorize("hasAuthority('defect.view')")
    public ResponseEntity<ApiResponse<List<DefectInProcess>>> countDefectInProcess(@PathVariable("productLineId") Long productLineId) {
        List<DefectInProcess> list = defectService.countDefectInProcess(productLineId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get defect coverage in product and its proportion")
    @GetMapping("/product-lines/{productLineId}/coverage")
    @PreAuthorize("hasAuthority('defect.view')")
    public ResponseEntity<ApiResponse<DefectCoverageResponse>> getCoverageInProductLine(@PathVariable("productLineId") Long productLineId) {
        DefectCoverageResponse result = defectService.getCoverageInProductLine(productLineId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get defects detail information")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('defect.view')")
    public ResponseEntity<ApiResponse<DefectResponse>> getDefectDetail(@PathVariable("id") Long id) {
        DefectResponse response = defectService.getDefectById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Validate duplicate defect Description")
    @GetMapping("/existence")
    @PreAuthorize("hasAuthority('defect.view')")
    public Boolean checkExistDefectDescription(@RequestParam String defectDescription) {
        return defectService.checkExistDefectDescription(defectDescription);
    }

    @Operation(summary = "Get defect proposals by productLine")
    @GetMapping("/proposals")
    @PreAuthorize("hasAuthority('defect_proposal.view')")
    public ResponseEntity<ApiResponse<List<DefectProposalResponse>>> getDefectProposalByProductLine(
            @RequestParam("productLineId") Long productLineId,
            @AuthenticationPrincipal User currentUser) {

        List<DefectProposalResponse> list = defectService.getDefectProposalByProductLine(productLineId, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Get defect proposal details")
    @GetMapping("/proposals/{id}/details")
    @PreAuthorize("hasAuthority('defect_proposal.view')")
    public ResponseEntity<ApiResponse<List<DefectProposalDetailResponse>>> getDefectProposalDetail(@PathVariable Long id) {
        List<DefectProposalDetailResponse> list = defectService.getDefectProposalDetails(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "Create new defect proposal")
    @PostMapping(value = "/proposals", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('defect_proposal.manage')")
    public ResponseEntity<ApiResponse<DefectProposalResponse>> createDefectProposal(
            @ModelAttribute DefectProposalRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(defectService.createDefectProposalDraft(request, currentUser)));
    }

    @DeleteMapping("/proposals/{id}")
    @PreAuthorize("hasAuthority('defect_proposal.manage')")
    public ResponseEntity<Void> deleteDefectProposal(@PathVariable("id") Long id) {
        defectService.deleteDefectProposal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/proposals/{id}")
    @PreAuthorize("hasAuthority('defect_proposal.manage')")
    public ResponseEntity<DefectProposalUpdateResponse> updateDefectProposal(
            @Parameter(description = "ID of the past defect proposal that needs to be corrected") @PathVariable Long id,
            @ModelAttribute DefectProposalRequest request, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        DefectProposalUpdateResponse response = defectService.updateDefectProposal(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Revising approval (Move to Draft)", description = "Move the proposal from the pending approval status back to the Draft status for further editing.")
    @PutMapping("/{id}/revise")
    @PreAuthorize("hasAuthority('defect_proposal.manage')")
    public ResponseEntity<String> revise(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        defectService.revise(id, currentUser, request);
        return ResponseEntity.ok("The proposal has been successfully moved back to the Draft status!");
    }

    @Operation(summary = "Approve defect proposal", description = "Approve the defect proposal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No approval permission"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Defect Proposal is not found")
    })
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('review_approve.review', 'review_approve.approve')")
    public ResponseEntity<String> approveProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApproveRequest approveRequest,
            HttpServletRequest request) {

        defectService.approve(id, currentUser, approveRequest, request);
        return ResponseEntity.ok("Defect Proposal has been approved successfully!");
    }

    @Operation(summary = "Reject defect proposal", description = "Reject and request revision of the defect proposal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Defect Proposal rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid rejection reason")
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('review_approve.review', 'review_approve.approve')")
    public ResponseEntity<String> rejectProposal(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RejectRequest rejectRequest,
            HttpServletRequest request) {

        defectService.reject(id, currentUser, rejectRequest, request);
        return ResponseEntity.ok("Defect Proposal has been rejected!");
    }

    @Operation(summary = "Check user approval permission for proposal")
    @GetMapping("/{id}/permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseEntity<Boolean>> getApprovePermission(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Proposal ID") @PathVariable Long id) {
        return ResponseEntity.ok(defectService.canApprove(id, currentUser));
    }

    @Operation(summary = "Import data (Defect Banking)")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('defect.manage')")
    public ResponseEntity<ApiResponse<List<DefectResponse>>> importDefect(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User currentUser) throws BadRequestException {
        List<DefectResponse> data = defectService.importDefect(currentUser, file);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Submit defect proposal for approval", description = "Change defect proposal status from DRAFT to SUBMITTED.")
    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('defect_proposal.manage')")
    public ResponseEntity<String> submit(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest request) {
        defectService.submitDefectProposalForApproval(id, currentUser, request);
        return ResponseEntity.ok("Defect proposal submitted for approval successfully!");
    }

    @Operation(summary = "Import Defect template")
    @GetMapping("/download-template")
    @PreAuthorize("hasAuthority('defect.manage')")
    public ResponseEntity<Resource> downloadTemplate() throws IOException {
        ClassPathResource file = new ClassPathResource("templates/excel/Defect_guideline.xlsx");

        if (!file.exists()) {
            throw new FileNotFoundException("Không tìm thấy file template Excel");
        }
        Resource resource = new InputStreamResource(file.getInputStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Defect_guideline.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .contentLength(file.contentLength())
                .body(resource);
    }

    @Operation(summary = "Get history import Defect")
    @GetMapping("/import-history")
    @PreAuthorize("hasAuthority('defect.manage')")
    public ResponseEntity<ApiResponse<List<ImportHistoryResponse>>> historyDefectImport(@AuthenticationPrincipal User currentUser) {
        List<ImportHistoryResponse> responses = importHistoryService.getHistory(currentUser, "DEFECT_IMPORT");
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
