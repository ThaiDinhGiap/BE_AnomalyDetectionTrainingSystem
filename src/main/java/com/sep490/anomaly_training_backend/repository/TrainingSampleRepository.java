package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleRepository extends JpaRepository<TrainingSample, Long> {
    List<TrainingSample> findByProcessId(Long processId);

    List<TrainingSample> findByProductLineId(Long productLineId);

    List<TrainingSample> findByProductLineIdAndDeleteFlagFalse(Long productLineId);

    Optional<TrainingSample> findByProductLineIdAndSampleCode(Long productLineId, String sampleCode);

    List<TrainingSample> findByDefectId(Long defectId);

    Boolean existsByProductLineIdAndSampleCode(Long productLineId, String sampleCode);

    Boolean existsByProductLineIdAndSampleCodeAndIdNot(Long productLineId, String sampleCode, Long id);
}
