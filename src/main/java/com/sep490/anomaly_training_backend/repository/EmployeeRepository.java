package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByTeamId(Long teamId);


    @Query("SELECT e FROM Employee e WHERE e.team.group.id = :groupId AND e.status = 'ACTIVE'")
    List<Employee> findAllActiveByGroupId(@Param("groupId") Long groupId);

}

