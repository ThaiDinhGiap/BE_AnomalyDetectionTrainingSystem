package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsByName(String name);
}

