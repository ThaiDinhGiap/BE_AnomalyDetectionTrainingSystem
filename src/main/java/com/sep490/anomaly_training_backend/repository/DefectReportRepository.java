package com.sep490.anomaly_training_backend.repository;
import com.sep490.anomaly_training_backend.enums.DefectReportStatus;
import com.sep490.anomaly_training_backend.model.DefectReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DefectReportRepository extends JpaRepository<DefectReport, Long> {
    List<DefectReport> findByDeleteFlagFalse();
    List<DefectReport> findByGroupIdAndDeleteFlagFalse(Long groupId);
    List<DefectReport> findByStatusAndDeleteFlagFalse(DefectReportStatus status);
}
