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

    @Modifying
    @Query(value = "UPDATE training_result_details SET created_by = :createdBy WHERE training_result_id = :resultId", nativeQuery = true)
    void updateCreatedByForResult(@Param("resultId") Long resultId, @Param("createdBy") String createdBy);

    Page<TrainingResultDetail> findByTrainingResultId(Long trainingResultId, Pageable pageable);

    List<TrainingResultDetail> findByTrainingResultId(Long trainingResultId);

    @Query("SELECT count(d) FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE d.actualDate IS NOT NULL " +
            "AND (:createdBy IS NULL OR r.createdBy = :createdBy) " +
            "AND (:teamId IS NULL OR r.team.id = :teamId) " +
            "AND (:lineId IS NULL OR r.line.id = :lineId) " +
            "AND (:year IS NULL OR r.year = :year)")
    long countByFilters(@Param("createdBy") String createdBy, @Param("teamId") Long teamId, @Param("lineId") Long lineId, @Param("year") Integer year);

    @Query("SELECT count(d) FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE d.actualDate IS NOT NULL " +
            "AND d.isPass = :isPass " +
            "AND (:createdBy IS NULL OR r.createdBy = :createdBy) " +
            "AND (:teamId IS NULL OR r.team.id = :teamId) " +
            "AND (:lineId IS NULL OR r.line.id = :lineId) " +
            "AND (:year IS NULL OR r.year = :year)")
    long countByFiltersAndIsPass(@Param("createdBy") String createdBy, @Param("teamId") Long teamId, @Param("lineId") Long lineId, @Param("year") Integer year, @Param("isPass") boolean isPass);


    @Query("SELECT d FROM TrainingResultDetail d JOIN d.trainingResult r " +
            "WHERE (d.status = 'NEED_SIGN') " +
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


    @Query("SELECT DISTINCT trd.employee.id FROM TrainingResultDetail trd " +
            "WHERE trd.trainingResult.id = :resultId AND trd.deleteFlag = false")
    List<Long> findEmployeeIdsByTrainingResultId(@Param("resultId") Long resultId);

    /**
     * Lấy tối đa N lần training gần nhất của 1 nhân viên cho 1 công đoạn.
     * Dùng để build recentHistory (6 dot lịch sử).
     * Chỉ lấy những record đã có actualDate (đã thực hiện).
     */
    @Query("""
                SELECT t FROM TrainingResultDetail t
                WHERE t.employee.id = :employeeId
                  AND t.process.id = :processId
                  AND t.actualDate IS NOT NULL
                  AND t.deleteFlag = false
                ORDER BY t.actualDate DESC
                LIMIT :limit
            """)
    List<TrainingResultDetail> findRecentByEmployeeAndProcess(
            @Param("employeeId") Long employeeId,
            @Param("processId") Long processId,
            @Param("limit") int limit);

    /**
     * Batch version: Lấy toàn bộ history của nhiều employee + process cùng lúc.
     * Dùng để tránh N+1 query khi render bảng chứng chỉ.
     */
    @Query("""
                SELECT t FROM TrainingResultDetail t
                WHERE t.employee.id IN :employeeIds
                  AND t.process IS NOT NULL
                  AND t.actualDate IS NOT NULL
                  AND t.deleteFlag = false
                ORDER BY t.employee.id, t.process.id, t.actualDate DESC
            """)
    List<TrainingResultDetail> findAllHistoryByEmployeeIds(
            @Param("employeeIds") List<Long> employeeIds);

    /**
     * Lấy các buổi huấn luyện của 1 employee trong 1 training result cụ thể.
     * Dùng cho section "Các buổi huấn luyện trong kế hoạch này".
     */
    @Query("""
                SELECT t FROM TrainingResultDetail t
                WHERE t.trainingResult.id = :resultId
                  AND t.employee.id = :employeeId
                  AND t.deleteFlag = false
                ORDER BY t.plannedDate ASC
            """)
    List<TrainingResultDetail> findByResultIdAndEmployeeId(
            @Param("resultId") Long resultId,
            @Param("employeeId") Long employeeId);

    /**
     * Batch: Lấy toàn bộ sessions của nhiều employee trong 1 result.
     * Tránh N+1 khi render tất cả nhân viên.
     */
    @Query("""
                SELECT t FROM TrainingResultDetail t
                LEFT JOIN FETCH t.process
                LEFT JOIN FETCH t.trainingSample
                LEFT JOIN FETCH t.signatureProOut
                LEFT JOIN FETCH t.signatureFiOut
                WHERE t.trainingResult.id = :resultId
                  AND t.deleteFlag = false
                ORDER BY t.employee.id, t.plannedDate ASC
            """)
    List<TrainingResultDetail> findAllSessionsByResultId(
            @Param("resultId") Long resultId);

    /**
     * Lấy các detail đang PENDING và đã có kết quả isPass (isPass IS NOT NULL)
     * theo training result id.
     */
    @Query("""
                SELECT t FROM TrainingResultDetail t
                WHERE t.trainingResult.id = :resultId
                  AND t.status = com.sep490.anomaly_training_backend.enums.ReportStatus.PENDING_REVIEW
                  AND t.isPass IS NOT NULL
                  AND t.deleteFlag = false
                ORDER BY t.plannedDate ASC
            """)
    List<TrainingResultDetail> findPendingWithIsPassByResultId(
            @Param("resultId") Long resultId);

    List<TrainingResultDetail> findAllByEmployeeIdAndDeleteFlagFalse(Long employeeId);

    @Query("""
                SELECT t FROM TrainingResultDetail t
                WHERE t.employee.id = :employeeId
                  AND t.status = com.sep490.anomaly_training_backend.enums.ReportStatus.COMPLETED
                  AND t.deleteFlag = false
                ORDER BY t.plannedDate ASC
            """)
    List<TrainingResultDetail> getTrainingHistory(@Param("employeeId") Long employeeId);
}
