package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ProposalStatus;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSampleProposalRepository extends JpaRepository<TrainingSampleProposal, Long> {
    List<TrainingSampleProposal> findByProductLineId(Long productLineId);

    List<TrainingSampleProposal> findByStatus(ProposalStatus status);

    List<TrainingSampleProposal> findByProductLineIdAndStatus(Long productLineId, ProposalStatus status);

    List<TrainingSampleProposal> findByDeleteFlagFalse();
}
