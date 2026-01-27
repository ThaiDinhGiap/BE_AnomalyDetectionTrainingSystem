package com.sep490.anomaly_training_backend.repository;
import com.sep490.anomaly_training_backend.model.Defect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {
    List<Defect> findByDeleteFlagFalse();
    List<Defect> findByProcessIdAndDeleteFlagFalse(Long processId);
}
