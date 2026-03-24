package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByTeamsId(Long teamId);

    List<Employee> findByTeamsIdAndDeleteFlagFalse(Long teamId);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.teams t WHERE t.teamLeader.id = :leaderId")
    List<Employee> findAllByTeamLeaderId(@Param("leaderId") Long leaderId);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.teams t WHERE t.id IN :teamIds")
    List<Employee> findAllByTeamIdIn(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.teams t WHERE t.group.id = :groupId AND e.status = :status")
    List<Employee> findAllActiveByGroupId(@Param("groupId") Long groupId, @Param("status") EmployeeStatus status);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.teams t WHERE t.id = :teamId AND e.status = :status")
    List<Employee> findAllActiveByTeamId(@Param("teamId") Long teamId, @Param("status") EmployeeStatus status);

    @Query("SELECT e FROM Employee e " +
            "LEFT JOIN User u ON e.employeeCode = u.employeeCode " +
            "WHERE u.id IS NULL AND e.deleteFlag = false")
    List<Employee> findAllEmployeesWithoutAccount();

    List<Employee> findByDeleteFlagFalse();
}