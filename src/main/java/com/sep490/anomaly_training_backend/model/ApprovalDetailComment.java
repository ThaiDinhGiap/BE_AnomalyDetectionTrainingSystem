package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity for approval_detail_comments table - Comments on detail rows when rejected
 */
@Entity
@Table(name = "approval_detail_comments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApprovalDetailComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_action_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ApprovalActionLog approvalAction;

    @Column(name = "entity_id", nullable = false)
    Long entityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User performedByUser;

    @Column(name = "entity_version", nullable = false)
    Integer entityVersion;

    @Column(name = "comment_description", columnDefinition = "text")
    String commentDescription;

    @Column(name = "performed_at", nullable = false)
    LocalDateTime performedAt;

    // Audit environment
    @Column(name = "ip_address", length = 45)
    String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    String userAgent;

    @Column(name = "device_info", length = 255)
    String deviceInfo;

    @Column(name = "content_hash", length = 64)
    String contentHash;
}
