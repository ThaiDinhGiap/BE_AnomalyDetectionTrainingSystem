package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ApprovalDetailComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalDetailCommentRepository extends JpaRepository<ApprovalDetailComment, Long> {
    List<ApprovalDetailComment> findByApprovalActionId(Long approvalActionId);

    List<ApprovalDetailComment> findByEntityId(Long entityId);

    List<ApprovalDetailComment> findByPerformedByUserId(Long userId);
}
