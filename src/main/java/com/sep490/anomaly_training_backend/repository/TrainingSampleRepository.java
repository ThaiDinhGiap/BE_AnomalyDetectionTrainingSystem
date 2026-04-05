package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.TrainingSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    SELECT MAX(CAST(SUBSTRING(ts.trainingCode, 3) AS long)) FROM TrainingSample ts WHERE ts.trainingCode LIKE 'TS%'
""")
    Optional<Long> findMaxTrainingCodeSequence();

    @Query("SELECT ts FROM TrainingSample ts")
    List<TrainingSample> findAllSamples();

    @Query("""
        SELECT DISTINCT ts FROM TrainingSample ts
        JOIN ts.products p
        WHERE p.id = :productId
    """)
    List<TrainingSample> findByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT DISTINCT ts FROM TrainingSample ts
        LEFT JOIN ts.products p
        WHERE p.id = :productId OR ts.products IS EMPTY
    """)
    List<TrainingSample> findByProductIdOrGlobal(@Param("productId") Long productId);

    /**
     * Check if a TrainingSample exists with matching fields AND contains ANY of the given products.
     * Uses JOIN through ManyToMany products collection.
     */
    @Query("""
    SELECT DISTINCT ts
    FROM TrainingSample ts
    JOIN ts.products p
    WHERE ts.process.id = :processId
      AND ts.categoryName = :categoryName
      AND ts.trainingDescription = :trainingDescription
      AND ts.trainingSampleCode = :trainingSampleCode
      AND p.id IN :productIds
""")
    Optional<TrainingSample> checkExist(
            @Param("processId") Long processId,
            @Param("categoryName") String categoryName,
            @Param("trainingDescription") String trainingDescription,
            @Param("trainingSampleCode") String trainingSampleCode,
            @Param("productIds") List<Long> productIds
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

    /**
     * Find all siblings in the same group (same trainingSampleCode + productLine)
     */
    List<TrainingSample> findByTrainingSampleCodeAndProductLineIdAndDeleteFlagFalse(
            String trainingSampleCode, Long productLineId);

    /**
     * Batch find all members of multiple groups (used for snapshot delete detection)
     */
    @Query("""
        SELECT ts FROM TrainingSample ts
        WHERE ts.trainingSampleCode IN :codes
          AND ts.productLine.id = :productLineId
          AND ts.deleteFlag = false
    """)
    List<TrainingSample> findByTrainingSampleCodeInAndProductLineIdAndDeleteFlagFalse(
            @Param("codes") Set<String> codes,
            @Param("productLineId") Long productLineId);
}
