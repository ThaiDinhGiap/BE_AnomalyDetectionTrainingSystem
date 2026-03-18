package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<TrainingResultDetail> findByTrainingResultId(Long trainingResultId, Pageable pageable);
    List<TrainingResultDetail> findByTrainingResultId(Long trainingResultId);
    @Query("SELECT count(d) FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE d.actualDate IS NOT NULL " +
            "AND (:teamId IS NULL OR r.team.id = :teamId) " +
            "AND (:lineId IS NULL OR r.line.id = :lineId) " +
            "AND (:year IS NULL OR r.year = :year)")
    long countByFilters(@Param("teamId") Long teamId, @Param("lineId") Long lineId, @Param("year") Integer year);

    @Query("SELECT count(d) FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE d.actualDate IS NOT NULL " +
            "AND d.isPass = :isPass " +
            "AND (:teamId IS NULL OR r.team.id = :teamId) " +
            "AND (:lineId IS NULL OR r.line.id = :lineId) " +
            "AND (:year IS NULL OR r.year = :year)")
    long countByFiltersAndIsPass(@Param("teamId") Long teamId, @Param("lineId") Long lineId, @Param("year") Integer year, @Param("isPass") boolean isPass);


    @Query("SELECT d FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE (d.status = 'NEED_SIGN' OR (d.actualDate IS NOT NULL AND d.signatureProOut IS NULL)) " +
            "AND (:lineId IS NULL OR r.line.id = :lineId)")
    List<TrainingResultDetail> findPendingSignatures(@Param("lineId") Long lineId);

    @Query("SELECT d FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE d.isPass = false AND (d.isRetrained = false OR d.isRetrained IS NULL) " +
            "AND (:lineId IS NULL OR r.line.id = :lineId)")
    List<TrainingResultDetail> findFailedTrainings(@Param("lineId") Long lineId);

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

    @Query("SELECT d.process.id, COUNT(d) FROM TrainingResultDetail d WHERE d.process.id IN :processIds AND d.status = 'DONE' GROUP BY d.process.id")
    List<Object[]> countCompletedByProcessIds(@Param("processIds") List<Long> processIds);

    @Query("SELECT d.process.id, COUNT(d) FROM TrainingResultDetail d WHERE d.process.id IN :processIds GROUP BY d.process.id")
    List<Object[]> countTotalByProcessIds(@Param("processIds") List<Long> processIds);

    @Query("""
                SELECT t FROM TrainingResultDetail t
                WHERE t.employee.id IN :employeeIds
                AND t.actualDate = (
                    SELECT MAX(t2.actualDate) FROM TrainingResultDetail t2
                    WHERE t2.employee.id = t.employee.id
                )
            """)
    List<TrainingResultDetail> findLatestByEmployeeIds(@Param("employeeIds") List<Long> employeeIds);
}
