package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicRepository extends JpaRepository<TrainingTopic, Long> {
    List<TrainingTopic> findByDeleteFlagFalse();

    List<TrainingTopic> findByProcessIdAndDeleteFlagFalse(Long processId);

    List<TrainingTopic> findByDefectIdAndDeleteFlagFalse(Long defectId);
    List<TrainingTopic> findByProcessId(Long processId);
}