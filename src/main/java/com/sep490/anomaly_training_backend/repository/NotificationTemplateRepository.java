package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByCodeAndDeleteFlagFalse(String code);
}

