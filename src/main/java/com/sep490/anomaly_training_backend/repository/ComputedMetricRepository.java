package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.model.ComputedMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComputedMetricRepository extends JpaRepository<ComputedMetric, Long> {

    List<ComputedMetric> findByEntityTypeAndIsActiveTrueAndDeleteFlagFalse(PolicyEntityType entityType);

    Optional<ComputedMetric> findByMetricNameAndDeleteFlagFalse(String metricName);
}
