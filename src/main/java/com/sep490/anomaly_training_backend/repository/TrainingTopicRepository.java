package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingTopicRepository extends JpaRepository<TrainingTopic, Long> {

    @Query("SELECT tt FROM TrainingTopic tt " +
            "WHERE tt.status = :status " +
            "AND tt.updatedAt < :threshold " +
            "AND tt.deleteFlag = false")
    List<TrainingTopic> findByStatusAndUpdatedAtBefore(
            @Param("status") String status,
            @Param("threshold") LocalDateTime threshold);
}