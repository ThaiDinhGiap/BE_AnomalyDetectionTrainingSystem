package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.DefectProposalDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectProposalDetailServiceImpl implements DefectProposalDetailService {
    private final DefectProposalDetailRepository DefectProposalDetailRepository;
    private final DefectProposalDetailMapper defectProposalDetailMapper;

    @Override
    public List<DefectProposalDetailResponse> getDefectProposalDetails(Long DefectProposalId) {
//        return DefectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(DefectProposalId)
//                                           .stream()
//                                           .map(DefectProposalDetailMapper::toResponse).toList();
        return null;
    }
}
