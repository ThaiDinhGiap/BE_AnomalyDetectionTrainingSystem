package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingTopicReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicReportHistoryRepository extends JpaRepository<TrainingTopicReportHistory, Long> {
    List<TrainingTopicReportHistory> findByTrainingTopicReportIdOrderByVersionDesc(Long trainingTopicReportId);
}
