package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingResultApprovalHandler implements ApprovalHandler {

    private final TrainingResultRepository trainingResultRepository;

    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.TRAINING_RESULT;
    }

    @Override
    public String getDisplayLabel() {
        return "kết quả huấn luyện";
    }

    @Override
    public List<OverdueItem> findOverdueItems(ReportStatus status, LocalDateTime threshold) {
        return trainingResultRepository.findByStatusAndDeleteFlagFalse(status)
                .stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .map(r -> new OverdueItem(r.getId(), r.getGroupId()))
                .toList();
    }

    @Override
    public void validateBeforeSubmit(Approvable entity) {
        if (entity.getStatus() != ReportStatus.ONGOING) {
            throw new AppException(ErrorCode.INVALID_ENTITY_STATUS,
                    "Result can only be submitted when in ONGOING status");
        }
    }

    @Override
    public void prepareForSubmit(Approvable entity) {
    }

    @Override
    public boolean requiresFlowStepOnSubmit() {
        return false;
    }

    @Override
    public boolean followsMultiStepFlow() {
        return false;
    }

    @Override
    public void afterApprove(Approvable entity) {
        entity.setCurrentVersion(entity.getCurrentVersion() + 1);
        log.info("TrainingResult id={} approved — version bumped to {}", entity.getId(), entity.getCurrentVersion());
    }

    @Override
    public void afterReject(Approvable entity) {
        log.info("TrainingResult id={} rejected — status stays ONGOING", entity.getId());
    }

    @Override
    public void applyApproval(Approvable entity) {
    }
}
