package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PrioritySnapshotDetailRepository extends JpaRepository<PrioritySnapshotDetail, Long> {

    void deleteBySnapshotId(Long snapshotId);

    @Modifying
    @Transactional
    void deleteByIdIn(List<Long> prioritySnapshotDetailIds);

    List<PrioritySnapshotDetail> findBySnapshotId(Long snapshotId);

    List<PrioritySnapshotDetail> findBySnapshotIdOrderByTierOrderAscSortRankAsc(Long snapshotId);
}
