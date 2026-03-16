package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Defect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {
    List<Defect> findByDeleteFlagFalse();

    @Query("SELECT d FROM Defect d " +
            "join d.process p " +
            "join p.productLine l " +
            "where l.id = :productLine and d.deleteFlag = false")
    List<Defect> findAllByProductLineAndDeleteFlagFalse(@Param("productLine") Long productLine);

    @Query("SELECT d FROM Defect d " +
            "join d.process p " +
            "join p.productLine l " +
            "join l.group g " +
            "join g.supervisor s " +
            "where s.id = :supervisorId and d.deleteFlag = false")
    List<Defect> findAllBySupervisorAndDeleteFlagFalse(@Param("supervisorId") Long supervisorId);

    @Query("""
    SELECT COUNT(d) > 0
    FROM Defect d
    WHERE LOWER(TRIM(d.defectDescription)) = LOWER(TRIM(:defectDescription))
      AND d.deleteFlag = false
""")
    boolean existsActiveByDefectDescriptionIgnoreCase(@Param("defectDescription") String defectDescription);

    Optional<Defect> findByDefectDescriptionIgnoreCase(String defectDescription);

    List<Defect> findByProcessIdAndDeleteFlagFalse(Long processId);

    Optional<Defect> findByDefectCode(String defectCode);

    @Query("SELECT MAX(CAST(SUBSTRING(d.defectCode, 3) AS long)) FROM Defect d WHERE d.defectCode LIKE 'DF%'")
    Optional<Long> findMaxDefectCodeSequence();
}
