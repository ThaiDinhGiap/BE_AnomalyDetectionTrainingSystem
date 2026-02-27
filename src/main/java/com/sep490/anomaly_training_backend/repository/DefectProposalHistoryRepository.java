package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.DefectProposalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectProposalHistoryRepository extends JpaRepository<DefectProposalHistory, Long> {
    List<DefectProposalHistory> findByDefectProposalId(Long defectProposalId);

    List<DefectProposalHistory> findByDefectProposalIdOrderByVersionDesc(Long defectProposalId);
}
