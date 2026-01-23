package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.NotificationQueueStatus;
import com.sep490.anomaly_training_backend.model.NotificationQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {

    List<NotificationQueue> findByStatusAndDeleteFlagFalse(NotificationQueueStatus status);

    List<NotificationQueue> findByRecipientUserIdAndDeleteFlagFalseOrderByCreatedAtDesc(Long recipientUserId);

    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = :status AND nq.scheduledAt <= :now AND nq.deleteFlag = false")
    List<NotificationQueue> findPendingToSend(@Param("status") NotificationQueueStatus status, @Param("now") Instant now);

    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'FAILED' AND nq.retryCount < nq.maxRetries AND nq.deleteFlag = false")
    List<NotificationQueue> findFailedForRetry();
}

