package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;

import com.sep490.anomaly_training_backend.model.TrainingSample;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleServiceImpl implements TrainingSampleService {

    private final TrainingSampleRepository trainingSampleRepository;
    private final TrainingSampleMapper trainingSampleMapper;

    @Override
    public List<TrainingSampleResponse> getTrainingSampleByProductLine(Long productLineId) {
        List<TrainingSample> listEntity = trainingSampleRepository.findByProductLineIdAndDeleteFlagFalse(productLineId);
        return listEntity.stream().map(trainingSampleMapper::toDto).toList();
    }
}
