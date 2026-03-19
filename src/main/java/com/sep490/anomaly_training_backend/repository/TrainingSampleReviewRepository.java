package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult;
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

    List<TrainingSampleReview> findByResult(TrainingSampleReviewResult result);

    List<TrainingSampleReview> findByReviewedById(Long reviewedById);

    List<TrainingSampleReview> findByDueDateBeforeAndResultEquals(LocalDate dueDate, TrainingSampleReviewResult result);

    List<TrainingSampleReview> findByConfigId(Long configId);

    List<TrainingSampleReview> findByProductLineId(Long productLineId);

    /**
     * Tìm tất cả review đã quá hạn chưa được phê duyệt
     *
     * Điều kiện:
     * - dueDate < ngày hôm nay
     * - result != APPROVED
     */
    @Query("SELECT tr FROM TrainingSampleReview tr " +
            "WHERE tr.dueDate < CURRENT_DATE " +
            "AND tr.productLine.id = :productLineId "+
            "AND tr.result != com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult.APPROVED")
    List<TrainingSampleReview> findOverdueReviews(@Param("productLineId") Long productLineId);

}
