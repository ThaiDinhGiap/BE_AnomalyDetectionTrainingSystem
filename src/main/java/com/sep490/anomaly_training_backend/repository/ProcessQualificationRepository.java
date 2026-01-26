package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ProcessQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessQualificationRepository extends JpaRepository<ProcessQualification, Long> {
    boolean existsByEmployeeIdAndProcessId(Long employeeId, Long processId);
    List<ProcessQualification> findByEmployeeId(Long employeeId);
    List<ProcessQualification> findByProcessId(Long processId);
}

