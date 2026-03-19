package com.sep490.anomaly_training_backend.service.sample.impl;

import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.dto.response.TrainingSampleProposalDetailResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TrainingSampleProposalDetailMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.sample.TrainingSampleProposalDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    @Override
    public List<TrainingSampleProposalDetailResponse> getTrainingSampleProposalDetails(Long trainingTopicReportId) {
        return trainingSampleProposalDetailRepository.findByTrainingSampleProposalIdAndDeleteFlagFalse(trainingTopicReportId)
                .stream()
                .map(this::enrichResponse)
                .toList();
    }

    // ── Save feedback ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void saveFeedback(Long detailId, DetailFeedbackRequest request, User currentUser) {

        TrainingSampleProposalDetail detail = trainingSampleProposalDetailRepository.findByIdAndDeleteFlagFalse(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_DETAIL_NOT_FOUND));

        // Tất cả null/empty → xoá feedback
        if (isEmptyFeedback(request)) {
            detail.setRejectFeedback(null);
            trainingSampleProposalDetailRepository.save(detail);
            return;
        }

        // Batch load reasons + action
        List<RejectFeedbackJson.RejectReasonSnapshot> reasonSnapshots = List.of();
        if (request.getRejectReasonIds() != null && !request.getRejectReasonIds().isEmpty()) {
            reasonSnapshots = rejectReasonRepository
                    .findAllById(request.getRejectReasonIds())
                    .stream()
                    .map(r -> RejectFeedbackJson.RejectReasonSnapshot.builder()
                            .id(r.getId())
                            .category(r.getCategoryName())
                            .label(r.getReasonName())
                            .build())
                    .toList();
        }

        RejectFeedbackJson.RequiredActionSnapshot actionSnapshot = null;
        if (request.getRequiredActionId() != null) {
            actionSnapshot = requiredActionRepository
                    .findById(request.getRequiredActionId())
                    .map(a -> RejectFeedbackJson.RequiredActionSnapshot.builder()
                            .id(a.getId())
                            .label(a.getActionName())
                            .build())
                    .orElse(null);
        }

        detail.setRejectFeedback(RejectFeedbackJson.builder()
                .savedAt(Instant.now())
                .savedBy(currentUser.getFullName())
                .rejectReasons(reasonSnapshots.isEmpty() ? null : reasonSnapshots)
                .requiredAction(actionSnapshot)
                .comment(request.getComment())
                .build());

        trainingSampleProposalDetailRepository.save(detail);
        log.info("[RejectFeedback] detailId={} updated by {}", detailId, currentUser.getUsername());
    }

    private boolean isEmptyFeedback(DetailFeedbackRequest r) {
        return (r.getRejectReasonIds() == null || r.getRejectReasonIds().isEmpty())
                && r.getRequiredActionId() == null
                && (r.getComment() == null || r.getComment().isBlank());
    }

    private TrainingSampleProposalDetailResponse addAttachment(TrainingSampleProposalDetailResponse response) {
        if (Objects.isNull(response)) {
            return null;
        }
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", response.getTrainingSampleId());
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
        return addAttachment(response);
    }
}
