package com.sep490.anomaly_training_backend.service.defect.impl;

import com.sep490.anomaly_training_backend.dto.response.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.defect.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefectProposalDetailServiceImpl implements DefectProposalDetailService {
    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final DefectProposalDetailMapper defectProposalDetailMapper;
    private final AttachmentService attachmentService;
    private final ProductService productService;

    @Override
    public List<DefectProposalDetailResponse> getDefectProposalDetails(Long defectProposalId) {
        List<DefectProposalDetail> responsesList = defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(defectProposalId);
        return responsesList.stream().map(detail -> {
            DefectProposalDetailResponse responseItem = defectProposalDetailMapper.toResponse(detail);
            if (detail.getProduct() != null) {
                responseItem.setProductResponse(productService.getProductById(detail.getProduct().getId()));
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
