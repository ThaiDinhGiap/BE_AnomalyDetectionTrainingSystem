package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.PriorityTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityTierRepository extends JpaRepository<PriorityTier, Long> {
    List<PriorityTier> findByPolicyIdAndDeleteFlagFalseOrderByTierOrder(Long policyId);

    void deleteByPolicyId(Long policyId);
}
