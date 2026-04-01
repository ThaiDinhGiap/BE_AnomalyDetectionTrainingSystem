package com.sep490.anomaly_training_backend.repository;


import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleReviewRepository extends JpaRepository<TrainingSampleReview, Long> {
    Optional<TrainingSampleReview> findByProductLineIdAndReviewDate(Long productLineId, LocalDate reviewDate);

    @Query("SELECT tr FROM TrainingSampleReview tr " +
            "WHERE tr.reviewedBy.id = :userId  " +
            "AND tr.status IN (com.sep490.anomaly_training_backend.enums.ReportStatus.ONGOING, com.sep490.anomaly_training_backend.enums.ReportStatus.REJECTED)" +
            "AND tr.productLine.id = :productLine  " +
            "AND tr.deleteFlag = false " +
            "ORDER BY tr.startDate DESC")
    List<TrainingSampleReview> findReviewTask(@Param("productLine") Long productLindId, @Param("userId") Long userId);

    List<TrainingSampleReview> findByConfigId(Long configId);

    List<TrainingSampleReview> findByProductLineIdOrderByCreatedAtDesc(Long productLineId);

    /**
     * Tìm tất cả review đã quá hạn chưa được phê duyệt
     * Điều kiện:
     * - dueDate < ngày hôm nay
     * - result != APPROVED
     */
    @Query("SELECT tr FROM TrainingSampleReview tr " +
            "WHERE tr.dueDate < CURRENT_DATE " +
            "AND tr.status != com.sep490.anomaly_training_backend.enums.ReportStatus.COMPLETED")
    List<TrainingSampleReview> findOverdueReviews();

    List<TrainingSampleReview> findByDeleteFlagFalse();

    @Query("SELECT tr FROM TrainingSampleReview tr " +
            "WHERE tr.productLine.id IN :lineIds " +
            "AND tr.status = com.sep490.anomaly_training_backend.enums.ReportStatus.PENDING_REVIEW " +
            "AND tr.deleteFlag = false")
    List<TrainingSampleReview> findPendingByLineIds(@Param("lineIds") List<Long> lineIds);

    @Query("""
            SELECT r FROM TrainingSampleReview r
            WHERE r.deleteFlag = false
              AND (:status IS NULL OR r.status = :status)
              AND (:productLineId IS NULL OR r.productLine.id = :productLineId)
              AND (CAST(:fromDate AS timestamp) IS NULL OR r.createdAt >= :fromDate)
              AND (CAST(:toDate AS timestamp) IS NULL OR r.createdAt <= :toDate)
              AND (:ids IS NULL OR r.id IN :ids)
            ORDER BY r.createdAt DESC
            """)
    List<TrainingSampleReview> findByExportFilters(
            @Param("status") ReportStatus status,
            @Param("productLineId") Long productLineId,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            @Param("ids") List<Long> ids);
}
