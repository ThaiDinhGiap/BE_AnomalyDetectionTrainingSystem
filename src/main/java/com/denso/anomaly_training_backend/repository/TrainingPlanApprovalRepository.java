package com.denso.anomaly_training_backend.repository;

import com.denso.anomaly_training_backend.model.Team;
import com.denso.anomaly_training_backend.model.TrainingPlanApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingPlanApprovalRepository extends JpaRepository<TrainingPlanApproval, Long> {
}
