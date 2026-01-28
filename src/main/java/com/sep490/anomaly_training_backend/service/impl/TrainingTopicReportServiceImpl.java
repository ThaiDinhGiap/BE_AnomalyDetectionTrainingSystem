package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingTopicReportMapper;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingTopicReport;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
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
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TrainingTopicReportMapper trainingTopicReportMapper;

    @Override
    public List<TrainingTopicReportResponse> getTrainingTopicReportsByUser(Long id) {
        return null;
    }
}
