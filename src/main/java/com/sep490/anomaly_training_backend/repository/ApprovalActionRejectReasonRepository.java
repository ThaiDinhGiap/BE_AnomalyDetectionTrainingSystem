package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ApprovalActionRejectReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalActionRejectReasonRepository extends JpaRepository<ApprovalActionRejectReason, Long> {
    List<ApprovalActionRejectReason> findByApprovalActionId(Long approvalActionId);

    List<ApprovalActionRejectReason> findByRejectReasonId(Long rejectReasonId);
}
