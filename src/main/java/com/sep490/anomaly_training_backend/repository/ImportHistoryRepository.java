package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
}
