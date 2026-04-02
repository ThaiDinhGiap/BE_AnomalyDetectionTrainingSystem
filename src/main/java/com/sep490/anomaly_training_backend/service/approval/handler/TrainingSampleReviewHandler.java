package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleReviewHandler implements ApprovalHandler {
    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.TRAINING_SAMPLE_REVIEW;
    }

    @Override
    public String getDisplayLabel() {
        return "báo cáo kiểm tra hàng năm";
    }

    @Override
    public void applyApproval(Approvable entity) {

    }
}
