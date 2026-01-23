package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByGroupId(Long groupId);

    boolean existsByName(String name);
}

