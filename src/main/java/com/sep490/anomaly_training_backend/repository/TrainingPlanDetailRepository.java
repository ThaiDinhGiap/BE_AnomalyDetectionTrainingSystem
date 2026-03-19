package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingPlanDetailRepository extends JpaRepository<TrainingPlanDetail, Long> {

    /**
     * Tìm training details theo ngày dự kiến và chưa thực hiện
     */
    @Query("SELECT tpd FROM TrainingPlanDetail tpd " +
            "JOIN FETCH tpd.employee e " +
            "JOIN FETCH tpd.trainingPlan tp " +
            "WHERE tpd.plannedDate = :date " +
            "AND tpd.status = 'PENDING' " +
            "AND tp.status = 'APPROVED' " +
            "AND tpd.deleteFlag = false")
    List<TrainingPlanDetail> findByPlannedDateAndResultStatusPending(@Param("date") LocalDate date);

    /**
     * Tìm training details quá hạn (planned_date < today và chưa ghi nhận)
     */
    @Query("SELECT tpd FROM TrainingPlanDetail tpd " +
            "JOIN FETCH tpd.employee e " +
            "JOIN FETCH tpd.trainingPlan tp " +
            "WHERE tpd. plannedDate < :today " +
            "AND tpd. status = 'PENDING' " +
            "AND tp. status = 'APPROVED' " +
            "AND tpd.deleteFlag = false " +
            "ORDER BY tpd.plannedDate ASC")
    List<TrainingPlanDetail> findOverdueTrainings(@Param("today") LocalDate today);

    /**
     * Lấy danh sách employee ID đã có trong plan
     */
    @Query("SELECT DISTINCT tpd.employee.id FROM TrainingPlanDetail tpd " +
            "WHERE tpd.trainingPlan.id = :planId AND tpd.deleteFlag = false")
    List<Long> findEmployeeIdsByTrainingPlanId(@Param("planId") Long planId);

    List<TrainingPlanDetail> findByTrainingPlanIdAndDeleteFlagFalse(Long planId);

    void deleteByTrainingPlanId(Long trainingPlanId);

    int countByTrainingPlanIdAndPlannedDate(Long trainingPlanId, LocalDate plannedDate);

    List<TrainingPlanDetail> findByTrainingPlanIdOrderByPlannedDateAsc(Long trainingPlanId);

    Optional<TrainingPlanDetail> findByIdAndDeleteFlagFalse(Long id);
}