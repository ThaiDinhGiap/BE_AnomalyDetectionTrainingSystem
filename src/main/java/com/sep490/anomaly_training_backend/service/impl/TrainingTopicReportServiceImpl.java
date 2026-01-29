package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingTopicReportMapper;
import com.sep490.anomaly_training_backend.model.TrainingTopicReport;
import com.sep490.anomaly_training_backend.repository.TrainingTopicReportRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingTopicReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TrainingTopicReportServiceImpl implements TrainingTopicReportService {
    private final TrainingTopicReportRepository trainingTopicReportRepository;
    private final TrainingTopicReportMapper trainingTopicReportMapper;
    private final UserRepository userRepository;

    @Override
    public List<TrainingTopicReportResponse> getTrainingTopicReportsByTeamLeadAndGroup(Long id, String username) {
        List<TrainingTopicReport> entityList= trainingTopicReportRepository.findByGroupIdAndCreatedByAndDeleteFlagFalse(id, username);
        List<TrainingTopicReportResponse> trainingTopicReportResponses = new ArrayList<>();
        for (TrainingTopicReport entity : entityList) {
            trainingTopicReportResponses.add(trainingTopicReportMapper.toResponse(entity, userRepository));
        }
        return  trainingTopicReportResponses;
    }
}
