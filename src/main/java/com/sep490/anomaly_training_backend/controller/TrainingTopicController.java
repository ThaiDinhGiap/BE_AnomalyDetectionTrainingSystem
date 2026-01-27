package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicResponse;
import com.sep490.anomaly_training_backend.service.TrainingTopicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-topics")
@RequiredArgsConstructor
@Tag(name = "Training Topic and Its Report Management", description = "API quản lý quy trình tạo báo cáo ghi xây dựng mẫu huấn luyện và view mẫu huấn luyện")
public class TrainingTopicController {

    public final TrainingTopicService trainingTopicService;

    @GetMapping("/")
    public ResponseEntity<List<TrainingTopicResponse>> getTrainingTopic(){
        List<TrainingTopicResponse> list = trainingTopicService.getTrainingTopics();
        return ResponseEntity.ok(list);
    }
}
