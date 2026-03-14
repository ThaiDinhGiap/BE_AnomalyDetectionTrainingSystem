package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.TrainingSampleReviewRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleReviewMapper;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import com.sep490.anomaly_training_backend.service.TrainingSampleReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleReviewServiceImpl implements TrainingSampleReviewService {
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;
    private final TrainingSampleReviewMapper trainingSampleReviewMapper;


    @Override
    public List<TrainingSampleReviewResponse> getTrainingSampleReviewByProductLine(Long productLineId) {
        return trainingSampleReviewRepository.findByProductLineId(productLineId)
                .stream()
                .map(trainingSampleReviewMapper::toDto)
                .toList();
    }

    @Override
    public TrainingSampleReviewResponse confirmReview(User reviewedBy, TrainingSampleReviewRequest request) {
        TrainingSampleReview reportReview = trainingSampleReviewRepository.findById(request.getId()).
                orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_REVIEW_NOT_FOUND));
        reportReview.setReviewedBy(reviewedBy);
        reportReview.setSampleSnapshot(request.getSampleSnapshot());
        trainingSampleReviewRepository.save(reportReview);
        return trainingSampleReviewMapper.toDto(reportReview);
    }
}