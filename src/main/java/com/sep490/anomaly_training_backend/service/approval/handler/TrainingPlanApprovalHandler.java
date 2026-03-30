package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingPlanApprovalHandler implements ApprovalHandler {

    private final TrainingResultService trainingResultService;
    private final TrainingPlanRepository trainingPlanRepository;

    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.TRAINING_PLAN;
    }

    @Override
    public String getDisplayLabel() {
        return "kế hoạch huấn luyện";
    }

    @Override
    public List<OverdueItem> findOverdueItems(ReportStatus status, LocalDateTime threshold) {
        return trainingPlanRepository.findByStatusAndUpdatedAtBefore(status, threshold)
                .stream()
                .map(p -> new OverdueItem(p.getId(), p.getGroupId()))
                .toList();
    }

    @Override
    public void applyApproval(Approvable entity) {
        log.info("TrainingPlan id={} approved — generating TrainingResult", entity.getId());
        trainingResultService.generateTrainingResult(entity.getId());
    }
}
