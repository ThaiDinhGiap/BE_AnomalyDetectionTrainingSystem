package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleReviewConfigRepository extends JpaRepository<TrainingSampleReviewConfig, Long> {
    Optional<TrainingSampleReviewConfig> findByProductLineIdAndDeleteFlagFalse(Long productLineId);

    List<TrainingSampleReviewConfig> findByIsActiveTrue();

    List<TrainingSampleReviewConfig> findByDeleteFlagFalse();
}
