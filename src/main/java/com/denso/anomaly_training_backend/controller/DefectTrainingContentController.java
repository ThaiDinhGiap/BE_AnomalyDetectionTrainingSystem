package com.denso.anomaly_training_backend.controller;

import com.denso.anomaly_training_backend.dto.response.DefectTrainingContentResponse;
import com.denso.anomaly_training_backend.service.DefectTrainingContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/defect-training-content")
@RequiredArgsConstructor
public class DefectTrainingContentController {
    private final DefectTrainingContentService defectTrainingContentService;

    @GetMapping("")
    public ResponseEntity<Page<DefectTrainingContentResponse>> getPageable(@RequestParam(required = false) Integer page,
                                                                              @RequestParam(required = false) Integer size) {
        Page<DefectTrainingContentResponse> result =  defectTrainingContentService.getAll(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DefectTrainingContentResponse>> getAll(){
        List<DefectTrainingContentResponse> result = defectTrainingContentService.getAllDefectTrainingContent();
        return ResponseEntity.ok(result);
    }
}
