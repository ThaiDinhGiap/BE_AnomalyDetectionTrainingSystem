package com.sep490.anomaly_training_backend.service.priority;

import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;

import java.util.List;

public interface PriorityScoringService {

    PrioritySnapshot generateSnapshot(Long policyId, Long teamId, List<Employee> employees);

    PrioritySnapshot recalculatePriorities(Long policyId, Long teamId);

    PrioritySnapshot getLatestSnapshot(Long policyId, Long teamId);

    void deleteSnapshot(Long snapshotId);

    List<PrioritySnapshot> listSnapshotsByPolicy(Long policyId);

    PrioritySnapshot getSnapshotById(Long snapshotId);
}