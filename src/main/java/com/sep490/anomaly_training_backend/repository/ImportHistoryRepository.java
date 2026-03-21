package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.ImportType;
import com.sep490.anomaly_training_backend.model.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    List<ImportHistory> findByUserIdAndImportTypeOrderByCreatedAtDesc(Long userId, ImportType importType);
}
