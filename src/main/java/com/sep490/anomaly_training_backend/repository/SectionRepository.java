package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsByName(String name);

    List<Section> findByManagerId(Long managerId);

    Optional<Section> findByCode(String code);

    List<Section> findByDeleteFlagFalse();
}

