package com.sep490.anomaly_training_backend.service.defect.impl;

import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalDetailResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.service.ProductService;
import com.sep490.anomaly_training_backend.service.defect.DefectProposalDetailService;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
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

    // ── Save feedback ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void saveFeedback(Long detailId, DetailFeedbackRequest request, User currentUser) {

        DefectProposalDetail detail = defectProposalDetailRepository.findByIdAndDeleteFlagFalse(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_DETAIL_NOT_FOUND));

        // Tất cả null/empty → xoá feedback
        if (isEmptyFeedback(request)) {
            detail.setRejectFeedback(null);
            defectProposalDetailRepository.save(detail);
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

        defectProposalDetailRepository.save(detail);
        log.info("[RejectFeedback] detailId={} updated by {}", detailId, currentUser.getUsername());
    }

    private boolean isEmptyFeedback(DetailFeedbackRequest r) {
        return (r.getRejectReasonIds() == null || r.getRejectReasonIds().isEmpty())
                && r.getRequiredActionId() == null
                && (r.getComment() == null || r.getComment().isBlank());
    }

    // ── Clear feedback (khi TL revise lại) ───────────────────────────────────

    @Override
    @Transactional
    public void clearFeedback(Long proposalId) {
        List<DefectProposalDetail> details =
                defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(proposalId);

        details.forEach(d -> d.setRejectFeedback(null));
        defectProposalDetailRepository.saveAll(details);

        log.info("[RejectFeedback] Đã xoá toàn bộ feedback của proposalId={}", proposalId);
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
