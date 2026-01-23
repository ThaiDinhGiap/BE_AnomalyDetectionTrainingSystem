package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findBySectionId(Long sectionId);
    boolean existsByName(String name);
    Optional<Group> findByIdAndDeleteFlagFalse(Long id);
}

