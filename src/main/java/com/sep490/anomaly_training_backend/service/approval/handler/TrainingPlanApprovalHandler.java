package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.util.ReportUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingPlanApprovalHandler implements ApprovalHandler {

    private final TrainingResultRepository trainingResultRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;

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
        TrainingPlan plan = trainingPlanRepository.findById(entity.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (!ReportStatus.COMPLETED.equals(plan.getStatus())) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        TrainingResult result = new TrainingResult();

        result.setTrainingPlan(plan);
        result.setTeam(plan.getTeam());
        result.setLine(plan.getLine());
        result.setYear(plan.getStartDate().getYear());
        result.setTitle("Báo cáo kết quả - " + plan.getTitle());
        result.setStatus(ReportStatus.ONGOING);
        result.setCurrentVersion(1);

        List<TrainingResultDetail> resultDetails = new ArrayList<>();

        if (plan.getDetails() != null) {
            for (TrainingPlanDetail planDetail : plan.getDetails()) {
                TrainingResultDetail resultDetail = new TrainingResultDetail();
                resultDetail.setTrainingResult(result);
                resultDetail.setTrainingPlanDetail(planDetail);
                resultDetail.setEmployee(planDetail.getEmployee());
                resultDetail.setPlannedDate(planDetail.getPlannedDate());
                resultDetail.setBatchId(planDetail.getBatchId());
                resultDetail.setStatus(ReportStatus.ONGOING);
                resultDetails.add(resultDetail);
            }
        }

        result.setDetails(resultDetails);
        TrainingResult savedResult = trainingResultRepository.save(result);

        String lineCode = plan.getLine() != null ? plan.getLine().getCode() : "";
        savedResult.setFormCode(
                ReportUtils.generateFormCode(ApprovalEntityType.TRAINING_RESULT, lineCode, savedResult.getId()));
        trainingResultRepository.save(savedResult);

        String planCreator = plan.getCreatedBy();
        trainingResultRepository.updateCreatedBy(savedResult.getId(), planCreator);
        trainingResultDetailRepository.updateCreatedByForResult(savedResult.getId(), planCreator);
    }
}
