package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLineRepository extends JpaRepository<ProductLine, Long> {
    List<ProductLine> findByGroupId(Long groupId);

    List<ProductLine> findByGroupIdAndDeleteFlagFalse(Long groupId);

    List<ProductLine> findByDeleteFlagFalse();
}
