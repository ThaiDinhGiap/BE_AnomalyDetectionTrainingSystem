package com.sep490.anomaly_training_backend.repository;


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
            "AND tr.status IN (com.sep490.anomaly_training_backend.enums.ReportStatus.PENDING, com.sep490.anomaly_training_backend.enums.ReportStatus.REJECTED_BY_SV)"+
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
            "AND tr.status != com.sep490.anomaly_training_backend.enums.ReportStatus.APPROVED")
    List<TrainingSampleReview> findOverdueReviews();

}
