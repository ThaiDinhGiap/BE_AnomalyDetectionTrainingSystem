package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.IssueDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueDetailRepository extends JpaRepository<IssueDetail, Long> {
}

