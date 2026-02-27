package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSampleProposalDetailRepository extends JpaRepository<TrainingSampleProposalDetail, Long> {
    List<TrainingSampleProposalDetail> findByTrainingSampleProposalId(Long trainingSampleProposalId);

    List<TrainingSampleProposalDetail> findByTrainingSampleProposalIdAndDeleteFlagFalse(Long trainingSampleProposalId);

    List<TrainingSampleProposalDetail> findByProcessId(Long processId);
}
