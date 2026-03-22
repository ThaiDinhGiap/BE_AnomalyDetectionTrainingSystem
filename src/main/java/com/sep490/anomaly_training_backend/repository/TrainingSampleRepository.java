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
    List<TrainingSample> findByProcessIdOrderByCreatedAtDesc(Long processId);

    List<TrainingSample> findByProcessIdAndDeleteFlagFalseOrderByCreatedAtDesc(Long processId);

    List<TrainingSample> findByCategoryNameAndDeleteFlagFalseOrderByCreatedAtDesc(String categoryName);

    List<TrainingSample> findByProductLineIdAndDeleteFlagFalseOrderByCreatedAtDesc(Long productLineId);

    @Query("SELECT ts FROM TrainingSample ts WHERE ts.productLine.id IN :lineIds AND ts.deleteFlag = false")
    List<TrainingSample> findByProductLineIdInAndDeleteFlagFalse(@Param("lineIds") List<Long> lineIds);


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

    @Query("""
    select ts
    from TrainingSample ts
    where ts.process.id = :processId
      and ts.categoryName = :categoryName
      and ts.trainingDescription = :trainingDescription
      and ts.product.id = :productId
      and ts.trainingSampleCode = :trainingSampleCode
""")
    Optional<TrainingSample> checkExist(
            @Param("processId") Long processId,
            @Param("categoryName") String categoryName,
            @Param("trainingDescription") String trainingDescription,
            @Param("productId") Long productId,
            @Param("trainingSampleCode") String trainingSampleCode
    );

    /**
     * Find maximum processOrder for a given process.
     * Used to determine next processOrder for a new process.
     */
    @Query("SELECT COALESCE(MAX(ts.processOrder), 0) FROM TrainingSample ts WHERE ts.process.id = :processId AND ts.deleteFlag = false")
    Integer findMaxProcessOrderByProcessId(@Param("processId") Long processId);

    /**
     * Find existing processOrder for a given process.
     * Assumes all TrainingSample with same processId have same processOrder (grouped by process).
     */
    @Query(value = "SELECT DISTINCT ts.process_order FROM training_samples ts WHERE ts.process_id = :processId AND ts.delete_flag = false LIMIT 1", nativeQuery = true)
    Optional<Integer> findProcessOrderByProcessId(@Param("processId") Long processId);

    /**
     * Find maximum categoryOrder for a given process and categoryName.
     */
    @Query("SELECT COALESCE(MAX(ts.categoryOrder), 0) FROM TrainingSample ts WHERE ts.process.id = :processId AND ts.categoryName = :categoryName AND ts.deleteFlag = false")
    Integer findMaxCategoryOrderByProcessAndCategory(@Param("processId") Long processId, @Param("categoryName") String categoryName);

    /**
     * Find existing categoryOrder for a given process and categoryName.
     */
    @Query(value = "SELECT DISTINCT ts.category_order FROM training_samples ts WHERE ts.process_id = :processId AND ts.category_name = :categoryName AND ts.delete_flag = false LIMIT 1", nativeQuery = true)
    Optional<Integer> findCategoryOrderByProcessAndCategory(@Param("processId") Long processId, @Param("categoryName") String categoryName);

    /**
     * Find maximum contentOrder for a given process, categoryName, and trainingDescription.
     */
    @Query("SELECT COALESCE(MAX(ts.contentOrder), 0) FROM TrainingSample ts WHERE ts.process.id = :processId AND ts.categoryName = :categoryName AND ts.trainingDescription = :trainingDescription AND ts.deleteFlag = false")
    Integer findMaxContentOrderByProcessCategoryAndDescription(@Param("processId") Long processId, @Param("categoryName") String categoryName, @Param("trainingDescription") String trainingDescription);

    /**
     * Find existing contentOrder for a given process, categoryName, and trainingDescription.
     */
    @Query(value = "SELECT DISTINCT ts.content_order FROM training_samples ts WHERE ts.process_id = :processId AND ts.category_name = :categoryName AND ts.training_description = :trainingDescription AND ts.delete_flag = false LIMIT 1", nativeQuery = true)
    Optional<Integer> findContentOrderByProcessCategoryAndDescription(@Param("processId") Long processId, @Param("categoryName") String categoryName, @Param("trainingDescription") String trainingDescription);
}
