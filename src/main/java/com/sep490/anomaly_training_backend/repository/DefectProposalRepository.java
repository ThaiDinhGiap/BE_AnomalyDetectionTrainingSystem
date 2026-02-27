package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ProposalStatus;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectProposalRepository extends JpaRepository<DefectProposal, Long> {
    List<DefectProposal> findByProductLineId(Long productLineId);

    List<DefectProposal> findByStatus(ProposalStatus status);

    List<DefectProposal> findByProductLineIdAndStatus(Long productLineId, ProposalStatus status);

    List<DefectProposal> findByDeleteFlagFalse();
}
