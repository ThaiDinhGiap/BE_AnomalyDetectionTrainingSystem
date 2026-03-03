package com.sep490.anomaly_training_backend.controller;


import com.sep490.anomaly_training_backend.dto.request.CreateDefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.DefectProposalService;
import com.sep490.anomaly_training_backend.service.DefectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defect and Defect Report Management", description = "API quản lý quy trình tạo báo cáo ghi nhận lỗi quá khứ cũng như lấy view Defect Banking")
public class DefectController {
    private final DefectService  defectService;
    private final DefectProposalService defectProposalService;
    private final DefectProposalDetailService defectProposalDetailService;

    @GetMapping("/")
    public ResponseEntity<List<DefectResponse>> getDefectByGroup(@RequestParam("groupId")Long groupId) {
        List<DefectResponse> list = defectService.getDefectByGroup(groupId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/proposal")
    public ResponseEntity<List<DefectProposalResponse>> getDefectProposalByGroup(@RequestParam("groupId")Long groupId,
                                                                       @AuthenticationPrincipal User currentUser) {
        List<DefectProposalResponse> list = defectProposalService.getDefectProposalByTeamLeadAndGroup(groupId, currentUser.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<List<DefectProposalDetailResponse>> getDefectProposalDetail(@PathVariable Long id) {
        List<DefectProposalDetailResponse> list = defectProposalDetailService.getDefectProposalDetails(id);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createDefectProposal(@RequestBody CreateDefectProposalRequest createDefectProposalRequest) {
        defectProposalService.createDefectProposal(createDefectProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
