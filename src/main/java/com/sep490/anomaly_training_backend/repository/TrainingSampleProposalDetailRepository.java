package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleProposalDetailRepository extends JpaRepository<TrainingSampleProposalDetail, Long> {
    List<TrainingSampleProposalDetail> findByTrainingSampleProposalId(Long trainingSampleProposalId);

    List<TrainingSampleProposalDetail> findByTrainingSampleProposalIdAndDeleteFlagFalse(Long trainingSampleProposalId);

    List<TrainingSampleProposalDetail> findByProcessId(Long processId);

    Optional<TrainingSampleProposalDetail> findByIdAndDeleteFlagFalse(Long id);
}
