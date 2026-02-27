package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingTopicReportDetailResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingTopicReportDetailMapper;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.TrainingTopicReportDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingTopicReportDetailServiceImpl implements TrainingTopicReportDetailService {
    private final TrainingSampleProposalDetailRepository trainingTopicReportDetailRepository;
    private final TrainingTopicReportDetailMapper  trainingTopicReportDetailMapper;

    @Override
    public List<TrainingTopicReportDetailResponse> getTrainingTopicReportDetails(Long trainingTopicReportId) {
//        return trainingTopicReportDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(trainingTopicReportId)
//                                                   .stream()
//                                                   .map(trainingTopicReportDetailMapper::toResponse).toList();
        return null;

    }
}
