package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.TrainingTopicReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicReportRepository extends JpaRepository<TrainingTopicReport, Long> {
    List<TrainingTopicReport> findByDeleteFlagFalse();

    List<TrainingTopicReport> findByGroupIdAndDeleteFlagFalse(Long groupId);

    List<TrainingTopicReport> findByStatusAndDeleteFlagFalse(ReportStatus status);
}
