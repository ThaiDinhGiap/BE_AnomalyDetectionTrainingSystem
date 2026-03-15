package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeIdAndDeleteFlagFalse(Long employeeId);

    List<EmployeeSkill> findByProcessIdAndDeleteFlagFalse(Long processId);

    Optional<EmployeeSkill> findByEmployeeIdAndProcessIdAndDeleteFlagFalse(Long employeeId, Long processId);

    @Query("SELECT es FROM EmployeeSkill es WHERE es.status = 'VALID' AND es.expiryDate BETWEEN CURRENT_DATE AND :thirtyDaysFromNow")
    List<EmployeeSkill> findExpiringSkills(@Param("thirtyDaysFromNow") LocalDate thirtyDaysFromNow);

    @Query("SELECT es FROM EmployeeSkill es " +
            "JOIN es.process p " +
            "WHERE es.employee.id = :employeeId " +
            "AND p.productLine.id = :lineId")
    List<EmployeeSkill> findSkillsByEmployeeAndLine(Long employeeId, Long lineId);
}
