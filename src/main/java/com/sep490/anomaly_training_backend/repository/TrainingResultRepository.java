package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingResultRepository extends JpaRepository<TrainingResult, Long> {

    @Modifying
    @Query(value = "UPDATE training_results SET created_by = :createdBy WHERE id = :id", nativeQuery = true)
    void updateCreatedBy(@Param("id") Long id, @Param("createdBy") String createdBy);

    @Query("SELECT tp FROM TrainingResult tp WHERE tp.team.group.id = :groupId AND tp.deleteFlag = false")
    List<TrainingResult> findByGroupId(Long groupId);

    @Query("SELECT tp FROM TrainingResult tp WHERE tp.team.group.id IN :groupIds AND tp.deleteFlag = false")
    List<TrainingResult> findByGroupIds(@Param("groupIds") List<Long> groupIds);

    List<TrainingResult> findByTrainingPlanIdIn(List<Long> planIds);

    List<TrainingResult> findByLineId(Long lineId);

    @Query("SELECT tr FROM TrainingResult tr WHERE tr.line.id = :lineId AND tr.deleteFlag = false")
    List<TrainingResult> findAllByLineIdForFinalInspection(@Param("lineId") Long lineId);

    @Query("SELECT tr FROM TrainingResult tr WHERE tr.deleteFlag = false")
    List<TrainingResult> findAllForFinalInspection();

    @Query("SELECT tr FROM TrainingResult tr " +
            "WHERE tr.line.id = :lineId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByLineIdExcludingStatuses(
            @Param("lineId") Long lineId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingResult tr " +
            "WHERE tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllExcludingStatuses(
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingResult tr " +
            "WHERE tr.createdBy = :createdBy " +
            "AND tr.line.id = :lineId " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByCreatedByAndLineId(
            @Param("createdBy") String createdBy,
            @Param("lineId") Long lineId);

    @Query("SELECT tr FROM TrainingResult tr " +
            "WHERE tr.createdBy = :createdBy " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByCreatedBy(@Param("createdBy") String createdBy);


    List<TrainingResult> findByLineIdAndDeleteFlagFalse(Long lineId);

    List<TrainingResult> findByCreatedByAndLineIdAndDeleteFlagFalse(String createdBy, Long lineId);

    List<TrainingResult> findByYear(Integer year);

    List<TrainingResult> findByStatus(ReportStatus status);


    List<TrainingResult> findByTrainingPlanId(Long trainingPlanId);

    List<TrainingResult> findByDeleteFlagFalse();

    List<TrainingResult> findByCreatedByAndDeleteFlagFalse(String createdBy);

    @Query("SELECT tr FROM TrainingResult tr LEFT JOIN FETCH tr.details WHERE tr.id = :id")
    Optional<TrainingResult> findByIdWithDetails(@Param("id") Long id);

    List<TrainingResult> findByStatusAndUpdatedAtBefore(
            @Param("status") ReportStatus status,
            @Param("threshold") LocalDateTime threshold);

    @Query("""
            SELECT pl
            FROM TrainingResult pl
            WHERE pl.line.id = :lineId
              AND pl.status NOT IN (com.sep490.anomaly_training_backend.enums.ReportStatus.DRAFT, com.sep490.anomaly_training_backend.enums.ReportStatus.REVISING)
              AND pl.deleteFlag = false
            """)
    List<TrainingResult> findByLineIdAndDeleteFlagFalseForSupervisorAndManager(
            @Param("lineId") Long lineId);

    @Query("""
            SELECT pl
            FROM TrainingResult pl
            WHERE pl.status NOT IN (com.sep490.anomaly_training_backend.enums.ReportStatus.DRAFT, com.sep490.anomaly_training_backend.enums.ReportStatus.REVISING)
              AND pl.deleteFlag = false
            """)
    List<TrainingResult> findByDeleteFlagFalseForSupervisorAndManager();

    @Query("SELECT tr FROM TrainingResult tr " +
            "JOIN tr.line.group.section s " +
            "WHERE s.manager.id = :managerId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByManager(
            @Param("managerId") Long managerId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingResult tr " +
            "JOIN tr.line.group.section s " +
            "WHERE s.manager.id = :managerId " +
            "AND tr.line.id = :lineId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByManagerAndLineId(
            @Param("managerId") Long managerId,
            @Param("lineId") Long lineId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingResult tr " +
            "JOIN tr.team t " +
            "WHERE t.teamLeader.id = :supervisorId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllBySupervisor(
            @Param("supervisorId") Long supervisorId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingResult tr " +
            "JOIN tr.team t " +
            "WHERE t.teamLeader.id = :supervisorId " +
            "AND tr.line.id = :lineId " +
            "AND tr.status NOT IN :excludedStatuses " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllBySupervisorAndLineId(
            @Param("supervisorId") Long supervisorId,
            @Param("lineId") Long lineId,
            @Param("excludedStatuses") List<ReportStatus> excludedStatuses);

    @Query("SELECT tr FROM TrainingResult tr " +
            "WHERE tr.line.group.id IN :groupIds " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByGroupIds(@Param("groupIds") List<Long> groupIds);

    @Query("SELECT tr FROM TrainingResult tr " +
            "WHERE tr.line.group.id IN :groupIds " +
            "AND tr.line.id = :lineId " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findAllByGroupIdsAndLineId(
            @Param("groupIds") List<Long> groupIds,
            @Param("lineId") Long lineId);

    List<TrainingResult> findByStatusAndDeleteFlagFalse(ReportStatus status);
}
