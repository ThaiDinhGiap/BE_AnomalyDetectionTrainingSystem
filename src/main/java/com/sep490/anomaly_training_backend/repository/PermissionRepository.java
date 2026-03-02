package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionCode(String permissionCode);

    boolean existsByPermissionCode(String permissionCode);

    List<Permission> findByModuleId(Long moduleId);

    @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.module ORDER BY p.sortOrder ASC")
    List<Permission> findAllWithModuleOrderBySortOrder();

    @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.deleteFlag = false")
    List<Permission> findByIdInAndDeleteFlagFalse(@Param("ids") Set<Long> ids);
}
