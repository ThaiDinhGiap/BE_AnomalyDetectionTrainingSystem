package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);
}
