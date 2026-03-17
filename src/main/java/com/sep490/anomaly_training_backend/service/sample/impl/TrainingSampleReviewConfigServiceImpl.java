package com.sep490.anomaly_training_backend.service.sample.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleReviewConfigResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleReviewConfigMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleReviewMapper;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleReviewConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleReviewConfigServiceImpl implements TrainingSampleReviewConfigService {
    private final TrainingSampleReviewConfigRepository trainingSampleReviewConfigRepository;
    private final TrainingSampleReviewConfigMapper trainingSampleReviewConfigMapper;
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;
    private final TrainingSampleReviewMapper trainingSampleReviewMapper;


    @Override
    public List<TrainingSampleReviewConfigResponse> getTrainingSampleReviewConfigByProductLine(Long productLineId) {
        return trainingSampleReviewConfigRepository.findByProductLineIdAndDeleteFlagFalse(productLineId)
                .stream()
                .map(trainingSampleReviewConfigMapper::toDto)
                .toList();
    }


}
