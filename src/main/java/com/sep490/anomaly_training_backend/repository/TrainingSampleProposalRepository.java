package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
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
              ORDER BY t.createdAt DESC
            """)
    List<TrainingSampleProposal> findByProductLineIdAndCreatedByOrderByCreatedAtDesc(
            @Param("productLineId") Long productLineId,
            @Param("username") String username);

    @Query("""
                SELECT sp FROM TrainingSampleProposal sp
                JOIN sp.productLine l
                JOIN l.group g
                JOIN g.section s
                WHERE sp.status = :status
                AND sp.deleteFlag = false
                ORDER BY sp.createdAt ASC
            """)
    List<TrainingSampleProposal> findPendingForApprove(
            @Param("status") ReportStatus status,
            @Param("userId") Long userId);

    @Query("""
            SELECT t
            FROM TrainingSampleProposal t
            WHERE t.productLine.id = :productLineId
              AND t.status NOT IN (com.sep490.anomaly_training_backend.enums.ReportStatus.DRAFT, com.sep490.anomaly_training_backend.enums.ReportStatus.REVISING)
              AND t.deleteFlag = false ORDER BY t.createdAt DESC
            """)
    List<TrainingSampleProposal> findByProductLineForSupervisorAndManagerOrderByCreatedAtDesc(@Param("productLineId") Long productLineId);

    List<TrainingSampleProposal> findByStatusAndDeleteFlagFalse(ReportStatus status);

    @Query("""
            SELECT t FROM TrainingSampleProposal t
            WHERE t.productLine.id IN :lineIds
              AND t.status = com.sep490.anomaly_training_backend.enums.ReportStatus.PENDING_REVIEW
              AND t.deleteFlag = false
            """)
    List<TrainingSampleProposal> findPendingByLineIds(@Param("lineIds") List<Long> lineIds);

    @Query("""
            SELECT t FROM TrainingSampleProposal t
            WHERE t.deleteFlag = false
              AND (:status IS NULL OR t.status = :status)
              AND (:productLineId IS NULL OR t.productLine.id = :productLineId)
              AND (CAST(:fromDate AS timestamp) IS NULL OR t.createdAt >= :fromDate)
              AND (CAST(:toDate AS timestamp) IS NULL OR t.createdAt <= :toDate)
              AND (:ids IS NULL OR t.id IN :ids)
              AND (:keyword IS NULL OR LOWER(t.formCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY t.createdAt DESC
            """)
    List<TrainingSampleProposal> findByExportFilters(
            @Param("status") ReportStatus status,
            @Param("productLineId") Long productLineId,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            @Param("ids") List<Long> ids,
            @Param("keyword") String keyword);
}
