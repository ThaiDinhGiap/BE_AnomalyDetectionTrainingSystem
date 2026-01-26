package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.TrainingTopicStatus;
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
@Table(name = "training_topics_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingTopicHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_topic_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingTopic trainingTopic;

    @Column(name = "version", nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_sv")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User verifiedBySv;

    @Column(name = "verified_at_sv")
    private Instant verifiedAtSv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_manager")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User approvedByManager;

    @Column(name = "approved_at_manager")
    private Instant approvedAtManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TrainingTopicStatus status;

    @Column(name = "recorded_at")
    private Instant recordedAt;
}

