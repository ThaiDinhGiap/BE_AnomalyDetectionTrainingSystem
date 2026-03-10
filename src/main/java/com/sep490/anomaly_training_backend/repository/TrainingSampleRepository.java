package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleRepository extends JpaRepository<TrainingSample, Long> {
    List<TrainingSample> findByProcessId(Long processId);

    List<TrainingSample> findByProcessIdAndDeleteFlagFalse(Long processId);

    List<TrainingSample> findByCategoryNameAndDeleteFlagFalse(String categoryName);

    List<TrainingSample> findByProductLineIdAndDeleteFlagFalse(Long productLineId);

    Optional<TrainingSample> findByProductLineIdAndTrainingSampleCode(Long productLineId, String trainingSampleCode);

    List<TrainingSample> findByDefectId(Long defectId);

    Boolean existsByProductLineIdAndTrainingSampleCode(Long productLineId, String trainingSampleCode);

    Boolean existsByProductLineIdAndTrainingSampleCodeAndIdNot(Long productLineId, String trainingSampleCode, Long id);
}
