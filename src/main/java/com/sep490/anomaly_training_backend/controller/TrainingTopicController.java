package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.CreateDefectReportRequest;
import com.sep490.anomaly_training_backend.dto.request.CreateTrainingTopicReportRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingTopicResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingTopicReportDetailService;
import com.sep490.anomaly_training_backend.service.TrainingTopicReportService;
import com.sep490.anomaly_training_backend.service.TrainingTopicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-topics")
@RequiredArgsConstructor
@Tag(name = "Training Topic and Its Report Management", description = "API quản lý quy trình tạo báo cáo ghi xây dựng mẫu huấn luyện và view mẫu huấn luyện")
public class TrainingTopicController {

    public final TrainingTopicService trainingTopicService;
    public final TrainingTopicReportService trainingTopicReportService;
    public final TrainingTopicReportDetailService trainingTopicReportDetailService;

    @GetMapping("/")
    public ResponseEntity<List<TrainingTopicResponse>> getTrainingTopicByGroup(@RequestParam("groupId") Long groupId){
        List<TrainingTopicResponse> list = trainingTopicService.getTrainingTopicsByGroup(groupId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/report")
    public ResponseEntity<List<TrainingTopicReportResponse>> getTrainingTopicReportByGroup(@RequestParam("groupId") Long groupId,
                                                                                           @AuthenticationPrincipal User currentUser){
        List<TrainingTopicReportResponse> list = trainingTopicReportService.getTrainingTopicReportsByTeamLeadAndGroup(groupId, "tl_prod01");
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<List<TrainingTopicReportDetailResponse>> getTrainingTopicDetail(@PathVariable Long id){
        List<TrainingTopicReportDetailResponse> list = trainingTopicReportDetailService.getTrainingTopicReportDetails(id);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createDefectReport(@RequestBody CreateTrainingTopicReportRequest createTrainingTopicReportRequest) {
        trainingTopicReportService.createTrainingTopicReport(createTrainingTopicReportRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
