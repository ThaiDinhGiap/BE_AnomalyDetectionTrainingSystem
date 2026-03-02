package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.PriorityTierFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityTierFilterRepository extends JpaRepository<PriorityTierFilter, Long> {

    List<PriorityTierFilter> findByTierIdAndDeleteFlagFalseOrderByFilterOrder(Long tierId);

    void deleteByTierId(Long tierId);
}
