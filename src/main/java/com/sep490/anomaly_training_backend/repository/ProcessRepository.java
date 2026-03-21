package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {
    boolean existsByCode(String code);

    List<Process> findByProductLineId(Long productLineId);

    List<Process> findByProductLineIdAndDeleteFlagFalse(Long productLineId);

    @Query("SELECT p FROM Process p WHERE p.productLine.id IN :lineIds AND p.deleteFlag = false")
    List<Process> findByProductLineIdInAndDeleteFlagFalse(@Param("lineIds") List<Long> lineIds);

    Optional<Process> findByProductLineIdAndCode(Long productLineId, String code);

    boolean existsByProductLineIdAndCode(Long productLineId, String code);

    Optional<Process> findByCode(String code);

    Optional<Process> findByProductLineCodeAndCode(String productLineCode, String processCode);
}
