package com.sep490.anomaly_training_backend.repository;

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

    /**
     * Tìm plans theo status và thời gian cập nhật (để check overdue)
     */
    @Query("SELECT tp FROM TrainingPlan tp " +
            "JOIN FETCH tp. group g " +
            "JOIN FETCH g.supervisor " +
            "JOIN FETCH g.section s " +
            "JOIN FETCH s.manager " +
            "WHERE tp.status = : status " +
            "AND tp.updatedAt < :threshold " +
            "AND tp.deleteFlag = false")
    List<TrainingPlan> findByStatusAndUpdatedAtBefore(
            @Param("status") String status,
            @Param("threshold") LocalDateTime threshold);

    @Query("SELECT p FROM TrainingPlan p LEFT JOIN FETCH p.details WHERE p.id = :id")
    Optional<TrainingPlan> findByIdWithDetails(Long id);
}