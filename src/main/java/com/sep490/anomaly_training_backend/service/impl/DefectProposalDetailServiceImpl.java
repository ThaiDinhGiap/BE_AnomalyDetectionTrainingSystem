package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectProposalDetailServiceImpl implements DefectProposalDetailService {
    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final DefectProposalDetailMapper defectProposalDetailMapper;
    private final AttachmentService attachmentService;

    @Override
    public List<DefectProposalDetailResponse> getDefectProposalDetails(Long defectProposalId) {
        List<DefectProposalDetailResponse> responsesList = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(defectProposalId)
                                           .stream()
                                           .map(defectProposalDetailMapper::toResponse).toList();
        for (DefectProposalDetailResponse responseItem : responsesList) {
            responseItem.setAttachments(attachmentService.getAttachmentsByEntity("DEFECT_PROPOSAL", responseItem.getId()));
        }
        return responsesList;
    }

}
