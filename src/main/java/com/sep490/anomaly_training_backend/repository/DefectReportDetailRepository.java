package com.sep490.anomaly_training_backend.repository;
import com.sep490.anomaly_training_backend.model.DefectReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DefectReportDetailRepository extends JpaRepository<DefectReportDetail, Long> {
    List<DefectReportDetail> findByDefectReportIdAndDeleteFlagFalse(Long defectReportId);
}
