package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.DefectReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectReportRepository extends JpaRepository<DefectReport, Long> {
    List<DefectReport> findByDeleteFlagFalse();

    List<DefectReport> findByGroupIdAndDeleteFlagFalse(Long groupId);

    List<DefectReport> findByStatusAndDeleteFlagFalse(ReportStatus status);

    @Query("""
                SELECT dr FROM DefectReport dr
                JOIN dr.group g
                JOIN g.section s
                WHERE dr.status = :status
                AND dr.deleteFlag = false
                AND (
                    (:role = 'SUPERVISOR' AND g.supervisor.id = :userId)
                    OR
                    (:role = 'MANAGER' AND s.manager.id = :userId)
                )
                ORDER BY dr.createdAt ASC
            """)
    List<DefectReport> findPendingForApprover(
            @Param("status") ReportStatus status,
            @Param("userId") Long userId,
            @Param("role") UserRole role);
    List<DefectReport> findByGroupIdAndCreatedByAndDeleteFlagFalse(Long groupId, String createdBy);
}
