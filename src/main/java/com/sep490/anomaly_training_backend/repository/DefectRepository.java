package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Defect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {
    List<Defect> findByDeleteFlagFalse();

    @Query("SELECT d FROM Defect d " +
            "join d.process p " +
            "join p.productLine l " +
            "join l.group g " +
            "where g.id = :groupId and d.deleteFlag = false")
    List<Defect> findAllByProductLineAndDeleteFlagFalse(@Param("groupId") Long groupId);

    @Query("SELECT d FROM Defect d " +
            "join d.process p " +
            "join p.productLine l " +
            "join l.group g " +
            "join g.supervisor s " +
            "where s.id = :supervisorId and d.deleteFlag = false")
    List<Defect> findAllBySupervisorAndDeleteFlagFalse(@Param("supervisorId") Long supervisorId);
}
