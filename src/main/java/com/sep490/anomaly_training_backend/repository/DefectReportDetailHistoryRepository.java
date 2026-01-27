package com.sep490.anomaly_training_backend.repository;
import com.sep490.anomaly_training_backend.model.DefectReportDetailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DefectReportDetailHistoryRepository extends JpaRepository<DefectReportDetailHistory, Long> {
    List<DefectReportDetailHistory> findByDefectReportHistoryId(Long defectReportHistoryId);
}
