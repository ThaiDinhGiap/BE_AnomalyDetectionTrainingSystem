package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    @Query("SELECT ns FROM NotificationSetting ns WHERE ns. template.code = :templateCode AND ns.deleteFlag = false")
    Optional<NotificationSetting> findByTemplateCode(@Param("templateCode") String templateCode);

    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.template.code = :templateCode AND ns.isEnabled = true AND ns.deleteFlag = false")
    Optional<NotificationSetting> findEnabledByTemplateCode(@Param("templateCode") String templateCode);
}

