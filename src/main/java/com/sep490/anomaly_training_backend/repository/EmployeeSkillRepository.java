package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeIdAndDeleteFlagFalse(Long employeeId);

    List<EmployeeSkill> findByProcessIdAndDeleteFlagFalse(Long processId);

    Optional<EmployeeSkill> findByEmployeeIdAndProcessIdAndDeleteFlagFalse(Long employeeId, Long processId);

//    List<EmployeeSkill> findByIsQualifiedTrueAndDeleteFlagFalse();

    List<EmployeeSkill> findByEmployeeIdAndProcessProductLineId(Long employeeId, Long productLineId);
}
