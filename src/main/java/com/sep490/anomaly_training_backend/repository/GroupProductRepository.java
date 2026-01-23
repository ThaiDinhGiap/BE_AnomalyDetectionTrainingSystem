package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.GroupProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupProductRepository extends JpaRepository<GroupProduct, Long> {
    boolean existsByGroupIdAndProductCode(Long groupId, String productCode);

    List<GroupProduct> findByGroupId(Long groupId);
}

