package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSampleRepository extends JpaRepository<TrainingSample, Long> {
    List<TrainingSample> findByProcessId(Long processId);

    List<TrainingSample> findByProcessIdAndDeleteFlagFalse(Long processId);

    List<TrainingSample> findByCategoryNameAndDeleteFlagFalse(String categoryName);

    List<TrainingSample> findByProductLineIdAndDeleteFlagFalse(Long productLineId);

    @Query("SELECT ts FROM TrainingSample ts WHERE ts.productLine.id IN :lineIds AND ts.deleteFlag = false")
    List<TrainingSample> findByProductLineIdInAndDeleteFlagFalse(@Param("lineIds") List<Long> lineIds);

    Optional<TrainingSample> findByProductLineIdAndTrainingSampleCode(Long productLineId, String trainingSampleCode);

    List<TrainingSample> findByDefectId(Long defectId);

    Boolean existsByProductLineIdAndTrainingSampleCode(Long productLineId, String trainingSampleCode);

    Boolean existsByProductLineIdAndTrainingSampleCodeAndIdNot(Long productLineId, String trainingSampleCode, Long id);

    @Query("SELECT DISTINCT t.categoryName FROM TrainingSample t WHERE t.productLine.id = :productLineId")
    List<String> getCategoryNames(@Param("productLineId") Long productLineId);

    @Query("SELECT DISTINCT t.trainingDescription FROM TrainingSample t WHERE t.productLine.id = :productLineId")
    List<String> getTrainingDescriptions(@Param("productLineId") Long productLineId);

    /**
     * Find TrainingSample by trainingCode (unique key for upsert in import)
     */
    Optional<TrainingSample> findByTrainingCode(String trainingCode);

    @Query(value = """
    SELECT MAX(CAST(SUBSTRING(training_code, 3) AS UNSIGNED))
    FROM training_samples
    WHERE training_code LIKE 'TS%'
      AND delete_flag = false
""", nativeQuery = true)
    Optional<Long> findMaxTrainingCodeSequence();

    List<TrainingSample> findByProductId(Long productId);
    @Query("SELECT ts FROM TrainingSample ts")
    List<TrainingSample> findAllSamples();

    @Query("SELECT ts FROM TrainingSample ts " +
            "WHERE ts.product.id = :productId OR ts.product IS NULL")
    List<TrainingSample> findByProductIdOrGlobal(@Param("productId") Long productId);

    List<TrainingSample> findByProductIdAndProcessId(Long productId, Long processId);
}
