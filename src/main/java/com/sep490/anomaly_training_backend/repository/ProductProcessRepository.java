package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ProductProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductProcessRepository extends JpaRepository<ProductProcess, Long> {
    List<ProductProcess> findByProductId(Long productId);

    List<ProductProcess> findByProcessId(Long processId);

    Optional<ProductProcess> findByProductIdAndProcessId(Long productId, Long processId);

    boolean existsByProductIdAndProcessId(Long productId, Long processId);
}
