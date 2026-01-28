package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.TrainingTopicReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicReportRepository extends JpaRepository<TrainingTopicReport, Long> {
    List<TrainingTopicReport> findByDeleteFlagFalse();

    List<TrainingTopicReport> findByGroupIdAndDeleteFlagFalse(Long groupId);

    List<TrainingTopicReport> findByStatusAndDeleteFlagFalse(ReportStatus status);

    @Query("""
                SELECT tr FROM TrainingTopicReport tr
                JOIN tr.group g
                JOIN g.section s
                WHERE tr.status = :status
                AND tr.deleteFlag = false
                AND (
                    (:role = 'SUPERVISOR' AND g.supervisor.id = :userId)
                    OR
                    (:role = 'MANAGER' AND s.manager.id = :userId)
                )
                ORDER BY tr.createdAt ASC
            """)
    List<TrainingTopicReport> findPendingForApprover(
            @Param("status") ReportStatus status,
            @Param("userId") Long userId,
            @Param("role") UserRole role);
}
