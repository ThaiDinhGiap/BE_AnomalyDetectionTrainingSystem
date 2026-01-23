package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.DefectTrainingContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefectTrainingContentRepository extends JpaRepository<DefectTrainingContent, Long> {
}

