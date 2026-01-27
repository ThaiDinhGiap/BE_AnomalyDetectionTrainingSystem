package com.sep490.anomaly_training_backend.repository;
import com.sep490.anomaly_training_backend.model.DefectReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DefectReportHistoryRepository extends JpaRepository<DefectReportHistory, Long> {
    List<DefectReportHistory> findByDefectReportIdOrderByVersionDesc(Long defectReportId);
}
