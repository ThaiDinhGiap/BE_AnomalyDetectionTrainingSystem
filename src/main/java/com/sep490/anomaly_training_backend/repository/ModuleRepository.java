package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByModuleCode(String moduleCode);

    boolean existsByModuleCode(String moduleCode);
}
