package com.sep490.anomaly_training_backend.controller;


import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.service.DefectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defect and Defect Report Management", description = "API quản lý quy trình tạo báo cáo ghi nhận lỗi quá khứ cũng như lấy view Defect Banking")
public class DefectController {

    private final DefectService  defectService;

    @GetMapping("/")
    public ResponseEntity<List<DefectResponse>> getTrainingTopic(){
        List<DefectResponse> list = defectService.getDefects();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{processId}")
    public ResponseEntity<List<DefectResponse>> getTrainingTopic(@PathVariable Long processId){
        List<DefectResponse> list = defectService.getDefectByProcess(processId);
        return ResponseEntity.ok(list);
    }

}
