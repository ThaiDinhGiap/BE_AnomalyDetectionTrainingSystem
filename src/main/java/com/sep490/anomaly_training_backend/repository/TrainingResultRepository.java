package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
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

    @Query("SELECT tp FROM TrainingResult tp WHERE tp.team.group.id = :groupId AND tp.deleteFlag = false")
    List<TrainingResult> findByGroupId(Long groupId);
    List<TrainingResult> findByTrainingPlanIdIn(List<Long> planIds);
    List<TrainingResult> findByLineId(Long lineId);

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
}
