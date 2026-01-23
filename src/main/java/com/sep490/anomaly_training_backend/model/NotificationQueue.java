package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.NotificationQueueStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "notification_queue")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class NotificationQueue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User recipientUser;

    @Column(name = "cc_list", columnDefinition = "TEXT")
    String ccList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_type", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    NotificationTemplate notificationTemplate;

    @Column(name = "related_entity_id")
    Long relatedEntityId;

    @Column(name = "related_entity_table", length = 50)
    String relatedEntityTable;

    @Column(nullable = false)
    String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    NotificationQueueStatus status = NotificationQueueStatus.PENDING;

    @Column(name = "retry_count")
    @Builder.Default
    Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    Integer maxRetries = 3;

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;

    @Column(name = "scheduled_at")
    Instant scheduledAt;

    @Column(name = "sent_at")
    Instant sentAt;
}