package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalFlowStepRepository extends JpaRepository<ApprovalFlowStep, Long> {

    List<ApprovalFlowStep> findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(ApprovalEntityType entityType);

    Optional<ApprovalFlowStep> findByEntityTypeAndApproverRoleAndIsActiveTrue(ApprovalEntityType entityType, UserRole approverRole);

    Optional<ApprovalFlowStep> findByEntityTypeAndStepOrderAndIsActiveTrue(ApprovalEntityType entityType, Integer stepOrder);
}