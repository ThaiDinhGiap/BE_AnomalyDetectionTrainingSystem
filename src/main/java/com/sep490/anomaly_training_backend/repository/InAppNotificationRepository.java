package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

    @Query("""
            SELECT n FROM InAppNotification n
            WHERE n.recipient.id = :recipientId
              AND n.isRead = false
              AND n.deleteFlag = false
            ORDER BY n.createdAt DESC
            LIMIT 20
            """)
    List<InAppNotification> findUnreadByRecipientId(@Param("recipientId") Long recipientId);

    @Query("""
            SELECT COUNT(n) FROM InAppNotification n
            WHERE n.recipient.id = :recipientId
              AND n.isRead = false
              AND n.deleteFlag = false
            """)
    Long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    @Query("""
            SELECT n FROM InAppNotification n
            WHERE n.recipient.id = :recipientId
              AND n.deleteFlag = false
            ORDER BY n.createdAt DESC
            """)
    Page<InAppNotification> findAllActiveByRecipientId(
            @Param("recipientId") Long recipientId,
            Pageable pageable);

    @Modifying
    @Query("""
            UPDATE InAppNotification n
            SET n.isRead = true, n.readAt = :now
            WHERE n.id = :id
              AND n.isRead = false
            """)
    int markAsRead(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            UPDATE InAppNotification n
            SET n.isRead = true, n.readAt = :now
            WHERE n.recipient.id = :recipientId
              AND n.isRead = false
            """)
    int markAllAsRead(@Param("recipientId") Long recipientId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            UPDATE InAppNotification n
            SET n.deleteFlag = true
            WHERE n.id = :id
              AND n.recipient.id = :recipientId
            """)
    int softDeleteByIdAndRecipient(@Param("id") Long id, @Param("recipientId") Long recipientId);
}
