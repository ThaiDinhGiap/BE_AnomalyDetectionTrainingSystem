package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingTopicReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicReportDetailRepository extends JpaRepository<TrainingTopicReportDetail, Long> {
    List<TrainingTopicReportDetail> findByTrainingTopicReportIdAndDeleteFlagFalse(Long trainingTopicReportId);
}
