package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingPlanSpecialDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrainingPlanSpecialDayRepository extends JpaRepository<TrainingPlanSpecialDay, Long> {
    @Modifying
    @Transactional
    void deleteByIdIn(List<Long> specialDayIds);
}
