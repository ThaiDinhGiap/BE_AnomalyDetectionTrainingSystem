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

    List<Employee> findByTeamIdAndDeleteFlagFalse(Long teamId);

    @Query("SELECT e FROM Employee e WHERE e.team.teamLeader.id = :leaderId")
    List<Employee> findAllByTeamLeaderId(@Param("leaderId") Long leaderId);

    List<Employee> findAllByTeamIdIn(List<Long> teamIds);

    @Query("SELECT e FROM Employee e WHERE e.team.group.id = :groupId AND e.status = 'ACTIVE'")
    List<Employee> findAllActiveByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT e FROM Employee e WHERE e.team.id = :teamId AND e.status = 'ACTIVE'")
    List<Employee> findAllActiveByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT e FROM Employee e " +
            "LEFT JOIN User u ON e.employeeCode = u.employeeCode " +
            "WHERE u.id IS NULL AND e.deleteFlag = false")
    List<Employee> findAllEmployeesWithoutAccount();
}
