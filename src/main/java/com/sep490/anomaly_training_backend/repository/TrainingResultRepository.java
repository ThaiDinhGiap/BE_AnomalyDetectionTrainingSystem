package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingResultRepository extends JpaRepository<TrainingResult, Long> {

    @Query("SELECT tr FROM TrainingResult tr " +
            "JOIN FETCH tr.group g " +
            "JOIN FETCH g.supervisor " +
            "JOIN FETCH g.section s " +
            "JOIN FETCH s.manager " +
            "WHERE tr.status = :status " +
            "AND tr.updatedAt < :threshold " +
            "AND tr.deleteFlag = false")
    List<TrainingResult> findByStatusAndUpdatedAtBefore(
            @Param("status") String status,
            @Param("threshold") LocalDateTime threshold);
}