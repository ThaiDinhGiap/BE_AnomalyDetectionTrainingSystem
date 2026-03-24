package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriorityPolicyRepository extends JpaRepository<PriorityPolicy, Long> {

    List<PriorityPolicy> findByStatusAndDeleteFlagFalse(PolicyStatus status);

    Optional<PriorityPolicy> findFirstByEntityTypeAndStatusAndDeleteFlagFalse(PolicyEntityType entityType, PolicyStatus status);

    List<PriorityPolicy> findByEntityTypeAndStatusAndDeleteFlagFalse(PolicyEntityType entityType, PolicyStatus status);

    Optional<PriorityPolicy> findByPolicyCodeAndDeleteFlagFalse(String policyCode);

    boolean existsByPolicyCodeAndDeleteFlagFalse(String policyCode);

    List<PriorityPolicy> findByDeleteFlagFalse();

    List<PriorityPolicy> findByEntityTypeAndDeleteFlagFalse(PolicyEntityType entityType);

    @Query("SELECT MAX(p.policyCode) FROM PriorityPolicy p WHERE p.entityType = :entityType AND p.policyCode LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxPolicyCodeByEntityTypeAndPrefix(@Param("entityType") PolicyEntityType entityType, @Param("prefix") String prefix);
}

