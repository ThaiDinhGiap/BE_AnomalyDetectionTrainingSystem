package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleMapper;
//import com.sep490.anomaly_training_backend.model.TrainingTopic;
//import com.sep490.anomaly_training_backend.repository.TrainingTopicRepository;
import com.sep490.anomaly_training_backend.service.TrainingSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleServiceImpl implements TrainingSampleService {

//    private final TrainingTopicRepository trainingTopicRepository;
    private final TrainingSampleMapper trainingSampleMapper;

    @Override
    public List<TrainingSampleResponse> getTrainingTopicsByGroup(Long groupId) {
//        List<TrainingTopic> listEntity = trainingTopicRepository.findByDeleteFlagFalse();
//        return listEntity.stream().map(trainingTopicMapper::toDto).toList();
        return null;
    }
}
