package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrioritySnapshotRepository extends JpaRepository<PrioritySnapshot, Long> {

    List<PrioritySnapshot> findByPolicyIdAndTeamId(Long policyId, Long teamId);

    PrioritySnapshot findLatestByPolicyIdAndTeamId(Long policyId, Long teamId);

    Optional<PrioritySnapshot> findByTrainingPlanId(Long trainingPlanId);
}
