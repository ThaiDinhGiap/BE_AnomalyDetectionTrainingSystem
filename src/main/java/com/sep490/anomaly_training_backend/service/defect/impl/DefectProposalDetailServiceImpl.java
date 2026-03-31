package com.sep490.anomaly_training_backend.service.defect.impl;

import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.defect.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefectProposalDetailServiceImpl implements DefectProposalDetailService {
    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final DefectProposalDetailMapper defectProposalDetailMapper;
    private final AttachmentService attachmentService;
    private final ProductService productService;
    private final RejectReasonRepository rejectReasonRepository;
    private final RequiredActionRepository requiredActionRepository;

    @Override
    public List<DefectProposalDetailResponse> getDefectProposalDetails(Long defectProposalId) {
        List<DefectProposalDetail> responsesList = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(defectProposalId);
        return responsesList.stream().map(detail -> {
            DefectProposalDetailResponse responseItem = defectProposalDetailMapper.toResponse(detail);
            if (detail.getProduct() != null) {
                responseItem.setProduct(productService.getProductById(detail.getProduct().getId()));
            }
            if (detail.getRejectFeedback() != null) {
                responseItem.setRejectFeedback(detail.getRejectFeedback());
            }
            return addAttachment(responseItem);
        }).toList();
    }

    private DefectProposalDetailResponse addAttachment(DefectProposalDetailResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("DEFECT_PROPOSAL", response.getDefectProposalDetailId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        response.setAttachmentUrls(imageUrls);
        return response;
    }

}
