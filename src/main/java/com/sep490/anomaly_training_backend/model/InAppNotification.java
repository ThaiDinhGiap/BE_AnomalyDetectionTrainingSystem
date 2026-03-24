package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
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

import java.time.LocalDateTime;

@Entity
@Table(
        name = "in_app_notifications"
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class InAppNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Người nhận — lazy load, không cần eager
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User recipient;

    @Column(nullable = false, length = 150)
    String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    String message;

    // Loại notification — FE tự map icon/màu từ field này
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    InAppNotificationType type = InAppNotificationType.INFO;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    Boolean isRead = false;

    @Column(name = "read_at")
    LocalDateTime readAt;

    // Deep link tới entity liên quan để FE có thể navigate
    @Column(name = "related_entity_type", length = 50)
    String relatedEntityType;   // e.g. "TRAINING_PLAN", "DEFECT_PROPOSAL"

    @Column(name = "related_entity_id")
    Long relatedEntityId;

    // URL tương đối, FE prefix baseUrl — e.g. "/training-plans/42"
    @Column(name = "action_url", length = 500)
    String actionUrl;
}
