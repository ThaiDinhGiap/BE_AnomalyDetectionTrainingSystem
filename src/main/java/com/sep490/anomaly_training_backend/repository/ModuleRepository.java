package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByModuleCode(String moduleCode);

    boolean existsByModuleCode(String moduleCode);

    @Query("SELECT DISTINCT m FROM Module m LEFT JOIN FETCH m.permissions ORDER BY m.sortOrder ASC")
    List<Module> findAllWithPermissions();
}
