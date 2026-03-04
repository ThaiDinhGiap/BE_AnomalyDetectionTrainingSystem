package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingResultDetailRepository extends JpaRepository<TrainingResultDetail, Long> {

    @Query("SELECT trd FROM TrainingResultDetail trd " +
            "JOIN FETCH trd.trainingPlanDetail tpd " +
            "JOIN FETCH tpd.employee " +
            "WHERE trd.updatedAt < :threshold " +
            "AND trd.deleteFlag = false")
    List<TrainingResultDetail> findByStatusAndUpdatedAtBefore(
            @Param("status") String status,
            @Param("threshold") LocalDateTime threshold);

    List<TrainingResultDetail> findByTrainingPlanDetailId(Long trainingPlanDetailId);

    @Modifying
    @Transactional
    void deleteByTrainingPlanDetailId(Long trainingPlanDetailId);

    @Modifying
    @Transactional
    void deleteByTrainingPlanDetailIdIn(List<Long> trainingPlanDetailIds);
}