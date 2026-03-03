package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSampleProposalRepository extends JpaRepository<TrainingSampleProposal, Long> {
    List<TrainingSampleProposal> findByProductLineId(Long productLineId);

    List<TrainingSampleProposal> findByStatus(ReportStatus status);

    List<TrainingSampleProposal> findByProductLineIdAndStatus(Long productLineId, ReportStatus status);

    List<TrainingSampleProposal> findByDeleteFlagFalse();
}
