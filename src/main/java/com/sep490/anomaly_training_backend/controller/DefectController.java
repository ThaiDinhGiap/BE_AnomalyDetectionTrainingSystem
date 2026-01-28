package com.sep490.anomaly_training_backend.controller;


import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.service.DefectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defect and Defect Report Management", description = "API quản lý quy trình tạo báo cáo ghi nhận lỗi quá khứ cũng như lấy view Defect Banking")
public class DefectController {

    private final DefectService  defectService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<DefectResponse>> getDefectBySupervisor(@PathVariable Long userId) {
        List<DefectResponse> list = defectService.getDefectBySupervisor(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/")
    public ResponseEntity<List<DefectResponse>> getDefectByGroup(@RequestParam("groupId")Long groupId) {
        List<DefectResponse> list = defectService.getDefectByGroup(groupId);
        return ResponseEntity.ok(list);
    }

}
