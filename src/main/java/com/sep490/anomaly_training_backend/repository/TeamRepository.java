package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByGroupId(Long groupId);

    List<Team> findByGroupIdIn(List<Long> groupIds);

    boolean existsByName(String name);

    @Query("SELECT t FROM Team t JOIN FETCH t.group WHERE t.teamLeader.id = :leaderId")
    List<Team> findAllByTeamLeaderId(@Param("leaderId") Long leaderId);

    Optional<Team> findByTeamLeader_Username(String username);

    List<Team> findByFinalInspectionId(Long finalInspectionId);

    Optional<Team> findByCode(String code);

    @Query("SELECT t FROM Team t " +
            "JOIN t.group g " +
            "JOIN g.section s " +
            "WHERE s.manager.id = :sectionManagerId AND t.deleteFlag = false")
    List<Team> findAllBySectionManagerId(@Param("sectionManagerId") Long sectionManagerId);

    @Query("SELECT t FROM Team t " +
            "JOIN t.group g " +
            "WHERE g.supervisor.id = :groupSupervisorId AND t.deleteFlag = false")
    List<Team> findAllByGroupSupervisorId(@Param("groupSupervisorId") Long groupSupervisorId);
}
