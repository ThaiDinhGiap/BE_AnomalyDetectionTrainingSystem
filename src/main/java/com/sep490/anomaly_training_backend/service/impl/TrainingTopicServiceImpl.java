package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingTopicMapper;
import com.sep490.anomaly_training_backend.model.TrainingTopic;
import com.sep490.anomaly_training_backend.repository.TrainingTopicRepository;
import com.sep490.anomaly_training_backend.service.TrainingTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTopicServiceImpl implements TrainingTopicService {

    private final TrainingTopicRepository trainingTopicRepository;
    private final TrainingTopicMapper trainingTopicMapper;

    @Override
    public List<TrainingTopicResponse> getTrainingTopicsByGroup(Long groupId) {
        List<TrainingTopic> listEntity = trainingTopicRepository.findByDeleteFlagFalse();
        return listEntity.stream().map(trainingTopicMapper::toDto).toList();
    }
}
