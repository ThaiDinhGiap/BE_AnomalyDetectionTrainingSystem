package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLineRepository extends JpaRepository<ProductLine, Long> {
    List<ProductLine> findByGroupId(Long groupId);

    List<ProductLine> findByGroupIdAndDeleteFlagFalse(Long groupId);

    List<ProductLine> findByDeleteFlagFalse();

    @Query("""
    SELECT DISTINCT p
    FROM User u
    JOIN Team t ON u.id = t.teamLeader.id
    JOIN Group g ON t.group.id = g.id
    JOIN ProductLine p ON p.group.id = g.id
    WHERE u.id = :teamLeadId
""")
    List<ProductLine> findProductLineByTeamLeadId(@Param("teamLeadId") Long teamLeadId);
}
