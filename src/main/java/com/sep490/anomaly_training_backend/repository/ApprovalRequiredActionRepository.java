package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ApprovalRequiredAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequiredActionRepository extends JpaRepository<ApprovalRequiredAction, Long> {
    List<ApprovalRequiredAction> findByApprovalActionId(Long approvalActionId);

    List<ApprovalRequiredAction> findByRequiredActionId(Long requiredActionId);
}
