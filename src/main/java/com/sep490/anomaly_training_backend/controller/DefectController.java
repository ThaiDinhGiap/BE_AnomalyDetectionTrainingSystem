package com.sep490.anomaly_training_backend.controller;


import com.sep490.anomaly_training_backend.dto.response.DefectReportDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectReportResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.DefectReportDetailService;
import com.sep490.anomaly_training_backend.service.DefectReportService;
import com.sep490.anomaly_training_backend.service.DefectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    private final DefectReportService defectReportService;
    private final DefectReportDetailService defectReportDetailService;

    @GetMapping("/")
    public ResponseEntity<List<DefectResponse>> getDefectByGroup(@RequestParam("groupId")Long groupId) {
        List<DefectResponse> list = defectService.getDefectByGroup(groupId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/report")
    public ResponseEntity<List<DefectReportResponse>> getDefectReportByGroup(@RequestParam("groupId")Long groupId,
                                                                       @AuthenticationPrincipal User currentUser) {
        List<DefectReportResponse> list = defectReportService.getDefectReportByTeamLeadAndGroup(groupId, currentUser.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<List<DefectReportDetailResponse>> getDefectReportDetail(@PathVariable Long id) {
        List<DefectReportDetailResponse> list = defectReportDetailService.getDefectReportDetails(id);
        return ResponseEntity.ok(list);
    }
}
