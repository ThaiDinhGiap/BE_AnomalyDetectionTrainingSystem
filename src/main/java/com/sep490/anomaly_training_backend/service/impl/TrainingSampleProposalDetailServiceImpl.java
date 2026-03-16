package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleResponse;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalDetailMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.TrainingSampleProposalDetailService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrainingSampleProposalDetailServiceImpl implements TrainingSampleProposalDetailService {
    private final TrainingSampleProposalDetailRepository TrainingSampleProposalDetailRepository;
    private final TrainingSampleProposalDetailMapper trainingSampleProposalDetailMapper;
    private final AttachmentService attachmentService;
    private final ProductService productService;

    @Override
    public List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId) {
        return TrainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(trainingTopicReportId)
                                                   .stream()
                                                   .map(trainingSampleProposalDetailMapper::toResponse).toList();
    }

    private TrainingSampleProposalDetailResponse addAttachment(TrainingSampleProposalDetailResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE", response.getTrainingSampleId());
        List<String> imageUrls = attachments.stream()
                .map(Attachment::getUrl)
                .toList();
        response.setAttachmentUrls(imageUrls);
        return response;
    }
    private TrainingSampleProposalDetailResponse enrichResponse(TrainingSampleProposalDetail entity) {
        TrainingSampleProposalDetailResponse response = trainingSampleProposalDetailMapper.toResponse(entity);
        if (entity.getProduct() != null) {
            response.setProduct(productService.getProductById(entity.getProduct().getId()));
        }
        return addAttachment(response);
    }
}
