package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.RejectReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RejectReasonRepository extends JpaRepository<RejectReason, Long> {
    List<RejectReason> findByCategoryName(String categoryName);

    List<RejectReason> findByDeleteFlagFalse();

    List<RejectReason> findAllByOrderByCategoryNameAscIdAsc();
}
