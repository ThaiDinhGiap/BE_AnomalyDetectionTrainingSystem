package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {

    @Query("SELECT ir FROM IssueReport ir " +
            "WHERE ir.status = :status " +
            "AND ir.updatedAt < :threshold " +
            "AND ir.deleteFlag = false")
    List<IssueReport> findByStatusAndUpdatedAtBefore(
            @Param("status") String status,
            @Param("threshold") LocalDateTime threshold);
}