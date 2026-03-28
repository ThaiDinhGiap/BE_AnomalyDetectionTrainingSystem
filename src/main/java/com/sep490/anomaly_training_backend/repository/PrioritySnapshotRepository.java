package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrioritySnapshotRepository extends JpaRepository<PrioritySnapshot, Long> {

    List<PrioritySnapshot> findByPolicyIdAndTeamId(Long policyId, Long teamId);

    @Query("SELECT s FROM PrioritySnapshot s WHERE s.policy.id = :policyId AND s.team.id = :teamId AND s.deleteFlag = false ORDER BY s.createdAt DESC LIMIT 1")
    PrioritySnapshot findLatestByPolicyIdAndTeamId(@Param("policyId") Long policyId, @Param("teamId") Long teamId);

    Optional<PrioritySnapshot> findByTrainingPlanId(Long trainingPlanId);

    @Query("SELECT s FROM PrioritySnapshot s WHERE s.policy.id = :policyId AND s.deleteFlag = false ORDER BY s.createdAt DESC")
    List<PrioritySnapshot> findByPolicyIdOrderByCreatedAtDesc(@Param("policyId") Long policyId);
}
