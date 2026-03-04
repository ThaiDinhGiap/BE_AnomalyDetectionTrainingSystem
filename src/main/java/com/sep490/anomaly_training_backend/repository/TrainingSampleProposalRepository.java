package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSampleProposalRepository extends JpaRepository<TrainingSampleProposal, Long> {
    List<TrainingSampleProposal> findByProductLineId(Long productLineId);

    List<TrainingSampleProposal> findByStatus(ReportStatus status);

    List<TrainingSampleProposal> findByProductLineIdAndStatus(Long productLineId, ReportStatus status);

    List<TrainingSampleProposal> findByDeleteFlagFalse();

    @Query("""
            SELECT t
            FROM TrainingSampleProposal t
            WHERE t.productLine.id = :productLineId
              AND t.createdBy = :username
              AND t.deleteFlag = false
            """)
    List<TrainingSampleProposal> findByProductLineIdAndCreatedBy(
            @Param("productLineId") Long productLineId,
            @Param("username") String username);

    @Query("""
                SELECT sp FROM TrainingSampleProposal sp
                JOIN sp.productLine l
                JOIN l.group g
                JOIN g.section s
                WHERE sp.status = :status
                AND sp.deleteFlag = false
                AND (
                    (:role = 'SUPERVISOR' AND g.supervisor.id = :userId)
                    OR
                    (:role = 'MANAGER' AND s.manager.id = :userId)
                )
                ORDER BY sp.createdAt ASC
            """)
    List<TrainingSampleProposal> findPendingForApprove(
            @Param("status") ReportStatus status,
            @Param("userId") Long userId,
            @Param("role") UserRole role);
}
