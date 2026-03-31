package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findBySectionId(Long sectionId);

    boolean existsByName(String name);

    Optional<Group> findByIdAndDeleteFlagFalse(Long id);

    List<Group> findBySupervisorId(Long supervisorId);

    @Query("SELECT DISTINCT g from Team t join t.group g where t.teamLeader.id = :teamLeadId AND g.deleteFlag = false")
    List<Group> findByTeamLeadId(@Param("teamLeadId") Long teamLeadId);

    Optional<Group> findByCode(String code);

    List<Group> findBySectionIdAndDeleteFlagFalse(Long sectionId);
}

