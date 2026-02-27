package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.DefectProposalDetailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectProposalDetailHistoryRepository extends JpaRepository<DefectProposalDetailHistory, Long> {
    List<DefectProposalDetailHistory> findByDefectProposalHistoryId(Long defectProposalHistoryId);
}
