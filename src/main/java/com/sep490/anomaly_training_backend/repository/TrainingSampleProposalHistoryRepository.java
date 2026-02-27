package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSampleProposalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSampleProposalHistoryRepository extends JpaRepository<TrainingSampleProposalHistory, Long> {
    List<TrainingSampleProposalHistory> findByTrainingSampleProposalId(Long trainingSampleProposalId);

    List<TrainingSampleProposalHistory> findByTrainingSampleProposalIdOrderByVersionDesc(Long trainingSampleProposalId);
}
