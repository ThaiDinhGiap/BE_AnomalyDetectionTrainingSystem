package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleReviewPolicyRepository extends JpaRepository<TrainingSampleReviewPolicy, Long> {

    /**
     * Tìm policy theo policy code
     */
    List<TrainingSampleReviewPolicy> findByProductLineIdAndDeleteFlagFalseOrderByCreatedByDesc(Long productLineId);

    /**
     * Kiểm tra tồn tại policy code
     */
    boolean existsByPolicyCodeAndDeleteFlagFalse(String policyCode);

    /**
     * Tìm tất cả policy theo status
     */
    List<TrainingSampleReviewPolicy> findByProductLineIdAndStatusAndDeleteFlagFalseOrderByCreatedAtDesc(Long productLineId, PolicyStatus status);

    /**
     * Tìm tất cả policy không bị xoá (pagination)
     */
    Page<TrainingSampleReviewPolicy> findByDeleteFlagFalse(Pageable pageable);

    /**
     * Tìm policy theo status (pagination)
     */
    Page<TrainingSampleReviewPolicy> findByStatusAndDeleteFlagFalse(PolicyStatus status, Pageable pageable);

    /**
     * Tìm policy có effective date <= ngày hiện tại và expiration_date >= ngày hiện tại
     */
    @Query("SELECT p FROM TrainingSampleReviewPolicy p WHERE p.deleteFlag = false " +
            "AND p.status = :status " +
            "AND p.effectiveDate <= :currentDate " +
            "AND (p.expirationDate IS NULL OR p.expirationDate >= :currentDate)")
    List<TrainingSampleReviewPolicy> findActivePoliciesByDate(
            @Param("status") PolicyStatus status,
            @Param("currentDate") LocalDate currentDate
    );

    @Query("SELECT p FROM TrainingSampleReviewPolicy p " +
            "WHERE p.deleteFlag = false AND p.status = com.sep490.anomaly_training_backend.enums.PolicyStatus.ACTIVE")
    List<TrainingSampleReviewPolicy> findByDeleteFlagFalseAndStatusActive();

    /**
     * Tìm policy có hiệu lực vào ngày nhất định (pagination)
     */
    @Query("SELECT p FROM TrainingSampleReviewPolicy p WHERE p.deleteFlag = false " +
            "AND p.status = :status " +
            "AND p.effectiveDate <= :targetDate " +
            "AND (p.expirationDate IS NULL OR p.expirationDate >= :targetDate)")
    Page<TrainingSampleReviewPolicy> findPoliciesByDateRange(
            @Param("status") PolicyStatus status,
            @Param("targetDate") LocalDate targetDate,
            Pageable pageable
    );

    /**
     * Tìm policy sắp hết hạn trong N ngày tới
     */
    @Query("SELECT p FROM TrainingSampleReviewPolicy p WHERE p.deleteFlag = false " +
            "AND p.status = :status " +
            "AND p.expirationDate IS NOT NULL " +
            "AND p.expirationDate BETWEEN :startDate AND :endDate")
    List<TrainingSampleReviewPolicy> findExpiringPolicies(
            @Param("status") PolicyStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Lấy policy code lớn nhất theo prefix để sinh code tự động
     */
    @Query("SELECT MAX(p.policyCode) FROM TrainingSampleReviewPolicy p " +
            "WHERE p.policyCode LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxPolicyCodeByPrefix(@Param("prefix") String prefix);
}

