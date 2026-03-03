package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.CreateTrainingSampleProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalDetailService;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalService;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-samples")
@RequiredArgsConstructor
@Tag(name = "Training Sample and Its Report Management", description = "API quản lý quy trình tạo báo cáo ghi xây dựng mẫu huấn luyện và view mẫu huấn luyện")
public class TrainingSampleController {

    public final TrainingSampleService trainingSampleService;
    public final TrainingSampleProposalService trainingSampleProposalService;
    public final TrainingSampleProposalDetailService trainingSampleProposalDetailService;

    @GetMapping("/")
    public ResponseEntity<List<TrainingSampleResponse>> getTrainingSampleByProductLine(@RequestParam("productLineId") Long productLineId){
        List<TrainingSampleResponse> list = trainingSampleService.getTrainingSampleByProductLine(productLineId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/proposal")
    public ResponseEntity<List<TrainingSampleProposalResponse>> getTrainingSampleProposalByProductLine(@RequestParam("productLineId") Long productLineId,
                                                                                                        @AuthenticationPrincipal User currentUser){
        List<TrainingSampleProposalResponse> list = trainingSampleProposalService.getTrainingSampleProposalsByTeamLeadAndProductLine(productLineId, currentUser.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<List<TrainingSampleProposalDetailResponse>> getTrainingSampleDetail(@PathVariable Long id){
        List<TrainingSampleProposalDetailResponse> list = trainingSampleProposalDetailService.getTrainingSampleProposalDetails(id);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createDefectProposal(@RequestBody CreateTrainingSampleProposalRequest createTrainingSampleProposalRequest) {
        trainingSampleProposalService.createTrainingSampleProposal(createTrainingSampleProposalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTrainingSampleProposal(@PathVariable("id") Long id){
        trainingSampleProposalService.deleteTrainingSampleProposal(id);
        return ResponseEntity.noContent().build();
    }
}
