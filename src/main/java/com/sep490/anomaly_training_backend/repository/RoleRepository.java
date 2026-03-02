package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    List<Role> findByDeleteFlagFalse();

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id AND r.deleteFlag = false")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);
}
