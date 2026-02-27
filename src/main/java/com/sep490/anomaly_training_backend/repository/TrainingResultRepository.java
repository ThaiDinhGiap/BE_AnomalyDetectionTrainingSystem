package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.TrainingResultStatus;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingResultRepository extends JpaRepository<TrainingResult, Long> {

    List<TrainingResult> findByGroupId(Long groupId);

    List<TrainingResult> findByLineId(Long lineId);

    List<TrainingResult> findByYear(Integer year);

    List<TrainingResult> findByStatus(TrainingResultStatus status);

    List<TrainingResult> findByGroupIdAndYear(Long groupId, Integer year);

    List<TrainingResult> findByTrainingPlanId(Long trainingPlanId);

    List<TrainingResult> findByDeleteFlagFalse();

    @Query("SELECT tr FROM TrainingResult tr LEFT JOIN FETCH tr.details WHERE tr.id = :id")
    Optional<TrainingResult> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT tr FROM TrainingResult tr " +
            "JOIN FETCH tr.group g " +
            "JOIN FETCH g.supervisor " +
            "JOIN FETCH g.section s " +
            "JOIN FETCH s.manager " +
            "WHERE tr.status = :status " +
            "AND tr.updatedAt < :threshold " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findByStatusAndUpdatedAtBefore(
            @Param("status") TrainingResultStatus status,
            @Param("threshold") LocalDateTime threshold);
}