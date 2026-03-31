package com.sep490.anomaly_training_backend.service.sample.impl;

import com.sep490.anomaly_training_backend.dto.response.sample.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalDetailMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.defect.DefectService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleProposalDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleProposalDetailServiceImpl implements TrainingSampleProposalDetailService {
    private final TrainingSampleProposalDetailRepository trainingSampleProposalDetailRepository;
    private final TrainingSampleProposalDetailMapper trainingSampleProposalDetailMapper;
    private final AttachmentService attachmentService;
    private final ProductService productService;
    private final RejectReasonRepository rejectReasonRepository;
    private final RequiredActionRepository requiredActionRepository;
    private final DefectService defectService;

    @Override
    public List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId) {
        return trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(trainingTopicReportId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    private TrainingSampleProposalDetailResponse addAttachment(TrainingSampleProposalDetailResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", response.getTrainingSampleProposalDetailId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        response.setAttachmentUrls(imageUrls);
        return response;
    }

    private TrainingSampleProposalDetailResponse enrichResponse(TrainingSampleProposalDetail entity) {
        TrainingSampleProposalDetailResponse response = trainingSampleProposalDetailMapper.toResponse(entity);
        if (entity.getRejectFeedback() != null) {
            response.setRejectFeedback(response.getRejectFeedback());
        }
        if (entity.getProduct() != null) {
            response.setProduct(productService.getProductById(entity.getProduct().getId()));
        }
        if (entity.getDefect() != null) {
            response.setDefect(defectService.getDefectById(entity.getDefect().getId()));
        }
        return addAttachment(response);
    }
}
