package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.IssueDetailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueDetailHistoryRepository extends JpaRepository<IssueDetailHistory, Long> {
}

