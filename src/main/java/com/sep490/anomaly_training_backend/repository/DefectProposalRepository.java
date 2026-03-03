package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectProposalRepository extends JpaRepository<DefectProposal, Long> {
    List<DefectProposal> findByProductLineId(Long productLineId);

    List<DefectProposal> findByStatus(ReportStatus status);

    List<DefectProposal> findByProductLineIdAndStatus(Long productLineId, ReportStatus status);

    List<DefectProposal> findByDeleteFlagFalse();

    @Query("""
       SELECT d 
       FROM DefectProposal d
       WHERE d.productLine.id = :productLineId
         AND d.createdBy = :username
         AND d.deleteFlag = false
       """)
    List<DefectProposal> findByProductLineIdAndCreatedBy(
            @Param("productLineId") Long productLineId,
            @Param("username") String username
    );
}
