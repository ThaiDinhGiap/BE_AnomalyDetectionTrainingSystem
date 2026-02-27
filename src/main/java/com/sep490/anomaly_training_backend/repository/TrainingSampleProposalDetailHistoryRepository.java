package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSampleProposalDetailHistoryRepository extends JpaRepository<TrainingSampleProposalDetailHistory, Long> {
    List<TrainingSampleProposalDetailHistory> findByTrainingSampleProposalHistoryId(Long trainingSampleProposalHistoryId);
}
