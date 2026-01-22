package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.TrainingTopicStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_topics")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingTopic extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private Integer version = 1;

    @Column(length = 200, nullable = false)
    private String title;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "verified_by_sv")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private User verifiedBySv;
//
//    @Column(name = "verified_at_sv")
//    private Instant verifiedAtSv;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "approved_by_manager")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private User approvedByManager;
//
//    @Column(name = "approved_at_manager")
//    private Instant approvedAtManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private TrainingTopicStatus status = TrainingTopicStatus.DRAFT;

    @Column(name = "current_version")
    @Builder.Default
    private Integer currentVersion = 1;

    @Column(name = "last_reject_reason", columnDefinition = "text")
    private String lastRejectReason;

    @OneToMany(mappedBy = "trainingTopic", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<TrainingTopicApproval> approvalLogs = new ArrayList<>();
}
