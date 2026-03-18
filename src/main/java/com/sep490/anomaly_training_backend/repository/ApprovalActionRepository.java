package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalActionRepository extends JpaRepository<ApprovalActionLog, Long> {

    List<ApprovalActionLog> findByEntityTypeAndEntityIdOrderByPerformedAtAsc(
            ApprovalEntityType entityType, Long entityId);

    List<ApprovalActionLog> findByEntityTypeAndEntityIdAndEntityVersionOrderByPerformedAtAsc(
            ApprovalEntityType entityType, Long entityId, Integer entityVersion);

    Optional<ApprovalActionLog> findByEntityTypeAndEntityIdAndEntityVersionAndStepOrder(
            ApprovalEntityType entityType, Long entityId, Integer entityVersion, Integer stepOrder);

    boolean existsByEntityTypeAndEntityIdAndEntityVersionAndAction(
            ApprovalEntityType entityType, Long entityId, Integer entityVersion, ApprovalAction action);

    @Query("SELECT a FROM ApprovalActionLog a WHERE a.performedByUser.id = :userId ORDER BY a.performedAt DESC")
    List<ApprovalActionLog> findByPerformedByUserIdOrderByPerformedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("""
            SELECT MAX(a.entityVersion)
            FROM ApprovalActionLog a
            WHERE a.entityType = :entityType
              AND a.entityId   = :entityId
              AND a.deleteFlag = false
            """)
    Optional<Integer> findMaxVersionByEntityTypeAndEntityId(
            @Param("entityType") ApprovalEntityType entityType,
            @Param("entityId") Long entityId);

    List<ApprovalActionLog> findByEntityTypeAndEntityIdAndEntityVersionAndDeleteFlagFalseOrderByStepOrderAsc(
            ApprovalEntityType entityType,
            Long entityId,
            Integer entityVersion);
}