package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingPlanDetailRepository extends JpaRepository<TrainingPlanDetail, Long> {

    /**
     * Tìm training details theo ngày dự kiến và chưa thực hiện
     */
    @Query("SELECT tpd FROM TrainingPlanDetail tpd " +
            "JOIN FETCH tpd.employee e " +
            "JOIN FETCH tpd.process p " +
            "JOIN FETCH tpd.trainingPlan tp " +
            "WHERE tpd.plannedDate = :date " +
            "AND tpd.resultStatus = 'PENDING' " +
            "AND tp.status = 'APPROVED' " +
            "AND tpd.deleteFlag = false")
    List<TrainingPlanDetail> findByPlannedDateAndResultStatusPending(@Param("date") LocalDate date);

    /**
     * Tìm training details quá hạn (planned_date < today và chưa ghi nhận)
     */
    @Query("SELECT tpd FROM TrainingPlanDetail tpd " +
            "JOIN FETCH tpd.employee e " +
            "JOIN FETCH tpd.process p " +
            "JOIN FETCH tpd.trainingPlan tp " +
            "WHERE tpd. plannedDate < :today " +
            "AND tpd. resultStatus = 'PENDING' " +
            "AND tp. status = 'APPROVED' " +
            "AND tpd.deleteFlag = false " +
            "ORDER BY tpd.plannedDate ASC")
    List<TrainingPlanDetail> findOverdueTrainings(@Param("today") LocalDate today);
}