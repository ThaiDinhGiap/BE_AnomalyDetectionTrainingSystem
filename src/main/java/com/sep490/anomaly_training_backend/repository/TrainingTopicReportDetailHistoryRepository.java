package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingTopicReportDetailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicReportDetailHistoryRepository extends JpaRepository<TrainingTopicReportDetailHistory, Long> {
    List<TrainingTopicReportDetailHistory> findByTrainingTopicReportHistoryId(Long trainingTopicReportHistoryId);
}
