package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {

    @Query("SELECT tp FROM TrainingPlan tp WHERE tp.team.group.id = :groupId AND tp.deleteFlag = false")
    List<TrainingPlan> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT tp FROM TrainingPlan tp WHERE tp.team.group.id IN :groupIds AND tp.deleteFlag = false")
    List<TrainingPlan> findByGroupIds(@Param("groupIds") List<Long> groupIds);

    List<TrainingPlan> findByIdIn(List<Long> ids);

    @Query("""
            SELECT pl
            FROM TrainingPlan pl
            WHERE pl.line.id = :lineId
              AND pl.status NOT IN (com.sep490.anomaly_training_backend.enums.ReportStatus.DRAFT, com.sep490.anomaly_training_backend.enums.ReportStatus.REVISING)
              AND pl.deleteFlag = false
            """)
    List<TrainingPlan> findByLineIdAndDeleteFlagFalseForSupervisorAndManager(Long lineId);

    @Query("""
            SELECT pl
            FROM TrainingPlan pl
            WHERE pl.status NOT IN (com.sep490.anomaly_training_backend.enums.ReportStatus.DRAFT, com.sep490.anomaly_training_backend.enums.ReportStatus.REVISING)
              AND pl.deleteFlag = false
            """)
    List<TrainingPlan> findByDeleteFlagFalseForSupervisorAndManager();

    List<TrainingPlan> findByLineIdAndDeleteFlagFalse(Long lineId);

    List<TrainingPlan> findByCreatedByAndLineIdAndDeleteFlagFalse(String createdBy, Long lineId);

    List<TrainingPlan> findByCreatedByAndDeleteFlagFalse(String createdBy);

    List<TrainingPlan> findByStatus(ReportStatus status);

    @Query("SELECT tp FROM TrainingPlan tp WHERE tp.team.group.id = :groupId AND tp.status = :status AND tp.deleteFlag = false")
    List<TrainingPlan> findByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") ReportStatus status);

    List<TrainingPlan> findByDeleteFlagFalse();

    List<TrainingPlan> findByStatusInAndDeleteFlagFalse(List<ReportStatus> statuses);

    List<TrainingPlan> findByTeamIdAndDeleteFlagFalse(Long teamId);

    /**
     * Tìm plans theo status và thời gian cập nhật (để check overdue)
     */
    @Query("SELECT tp FROM TrainingPlan tp " +
            "JOIN FETCH tp.line l " +
            "JOIN FETCH l.group g " +
            "JOIN FETCH g.supervisor " +
            "JOIN FETCH g.section s " +
            "JOIN FETCH s.manager " +
            "WHERE tp.status = :status " +
            "AND tp.updatedAt < :threshold " +
            "AND tp.deleteFlag = false")
    List<TrainingPlan> findByStatusAndUpdatedAtBefore(
            @Param("status") ReportStatus status,
            @Param("threshold") LocalDateTime threshold);

    @Query("SELECT p FROM TrainingPlan p LEFT JOIN FETCH p.details WHERE p.id = :id")
    Optional<TrainingPlan> findByIdWithDetails(@Param("id") Long id);

    @Query("""
                SELECT tr FROM TrainingPlan tr
                JOIN tr.line l
                JOIN l.group g
                JOIN g.section s
                WHERE tr.status = :status
                AND tr.deleteFlag = false
                ORDER BY tr.createdAt ASC
            """)
    List<TrainingPlan> findPendingForApprove(
            @Param("status") ReportStatus status,
            @Param("userId") Long userId);

    @Query("SELECT tr FROM TrainingPlan tr " +
            "WHERE tr.line.group.id IN :groupIds " +
            "AND tr.line.id = :lineId " +
            "AND tr.deleteFlag = false")
    List<TrainingPlan> findAllByGroupIdsAndLineIdAndDeleteFlagFalse(
            @Param("groupIds") List<Long> groupIds,
            @Param("lineId") Long lineId);

    @Query("SELECT tr FROM TrainingPlan tr " +
            "WHERE tr.line.group.id IN :groupIds " +
            "AND tr.deleteFlag = false")
    List<TrainingPlan> findAllByGroupIdsAndDeleteFlagFalse(@Param("groupIds") List<Long> groupIds);

    @Query("SELECT tr FROM TrainingPlan tr " +
            "JOIN tr.line.group.section s " +
            "WHERE s.manager.id = :managerId " +
            "AND tr.line.id = :lineId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingPlan> findAllByManagerAndLineIdAndDeleteFlagFalse(
            @Param("managerId") Long managerId,
            @Param("lineId") Long lineId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingPlan tr " +
            "JOIN tr.line.group.section s " +
            "WHERE s.manager.id = :managerId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingPlan> findAllByManagerAndDeleteFlagFalse(
            @Param("managerId") Long managerId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("""
            SELECT p FROM TrainingPlan p
            WHERE p.deleteFlag = false
              AND (:status IS NULL OR p.status = :status)
              AND (:productLineId IS NULL OR p.line.id = :productLineId)
              AND (:teamId IS NULL OR p.team.id = :teamId)
              AND (CAST(:fromDate AS timestamp) IS NULL OR p.createdAt >= :fromDate)
              AND (CAST(:toDate AS timestamp) IS NULL OR p.createdAt <= :toDate)
              AND (:ids IS NULL OR p.id IN :ids)
              AND (:keyword IS NULL OR LOWER(p.formCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.createdAt DESC
            """)
    List<TrainingPlan> findByExportFilters(
            @Param("status") ReportStatus status,
            @Param("productLineId") Long productLineId,
            @Param("teamId") Long teamId,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            @Param("ids") List<Long> ids,
            @Param("keyword") String keyword);
}