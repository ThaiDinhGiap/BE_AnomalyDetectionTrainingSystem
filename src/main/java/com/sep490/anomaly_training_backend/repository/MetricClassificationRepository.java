package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.MetricClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricClassificationRepository extends JpaRepository<MetricClassification, Long> {

    List<MetricClassification> findByClassificationNameAndIsActiveTrueOrderByPriority(String classificationName);
}
