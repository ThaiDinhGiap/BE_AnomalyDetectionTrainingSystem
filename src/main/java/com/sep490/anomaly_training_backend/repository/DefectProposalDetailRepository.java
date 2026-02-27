package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectProposalDetailRepository extends JpaRepository<DefectProposalDetail, Long> {
    List<DefectProposalDetail> findByDefectProposalId(Long defectProposalId);

    List<DefectProposalDetail> findByDefectProposalIdAndDeleteFlagFalse(Long defectProposalId);

    List<DefectProposalDetail> findByProcessId(Long processId);
}
