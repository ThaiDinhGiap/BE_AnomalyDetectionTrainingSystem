package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.RequiredAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequiredActionRepository extends JpaRepository<RequiredAction, Long> {
    List<RequiredAction> findByDeleteFlagFalse();
}
