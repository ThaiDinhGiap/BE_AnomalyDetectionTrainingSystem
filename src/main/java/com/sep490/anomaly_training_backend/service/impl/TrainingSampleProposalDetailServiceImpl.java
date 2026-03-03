package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalDetailMapper;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSampleProposalDetailServiceImpl implements TrainingSampleProposalDetailService {
    private final TrainingSampleProposalDetailRepository TrainingSampleProposalDetailRepository;
    private final TrainingSampleProposalDetailMapper trainingSampleProposalDetailMapper;

    @Override
    public List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId) {
        return TrainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(trainingTopicReportId)
                                                   .stream()
                                                   .map(trainingSampleProposalDetailMapper::toResponse).toList();
    }
}
