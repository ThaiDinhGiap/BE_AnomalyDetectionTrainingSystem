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

    @Query("SELECT es FROM EmployeeSkill es JOIN es.process p " +
            "WHERE es.status = 'VALID' AND es.expiryDate BETWEEN CURRENT_DATE AND :thirtyDaysFromNow " +
            "AND (:lineId IS NULL OR p.productLine.id = :lineId)")
    List<EmployeeSkill> findExpiringSkills(@Param("lineId") Long lineId, @Param("thirtyDaysFromNow") LocalDate thirtyDaysFromNow);

    @Query("SELECT es FROM EmployeeSkill es " +
            "JOIN es.process p " +
            "WHERE es.employee.id = :employeeId " +
            "AND p.productLine.id = :lineId")
    List<EmployeeSkill> findSkillsByEmployeeAndLine(Long employeeId, Long lineId);

    List<EmployeeSkill> findByEmployeeIdIn(List<Long> employeeIds);

    /**
     * Batch load skills của nhiều employee, filter theo productLine.
     * Fetch process luôn để tránh lazy load N+1.
     */
    @Query("""
                SELECT es FROM EmployeeSkill es
                LEFT JOIN FETCH es.process p
                WHERE es.employee.id IN :employeeIds
                  AND p.productLine.id = :lineId
                  AND es.deleteFlag = false
                ORDER BY es.employee.id, p.id
            """)
    List<EmployeeSkill> findByEmployeeIdsAndLineId(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("lineId") Long lineId);
}
