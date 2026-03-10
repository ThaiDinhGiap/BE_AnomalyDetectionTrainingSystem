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

    List<TrainingPlan> findByLineIdAndDeleteFlagFalse(Long lineId);

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
}