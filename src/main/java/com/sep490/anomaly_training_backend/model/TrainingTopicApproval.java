package com.sep490.anomaly_training_backend.model;


import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.TrainingTopicStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "training_topic_approval")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingTopicApproval extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_topic_id", nullable = false)
    @ToString.Exclude
    private TrainingTopic trainingTopic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processed_by_user_id", nullable = false)
    @ToString.Exclude
    private User processedBy;

    @Column(name = "processed_role")
    private String processedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_status")
    private TrainingTopicStatus resultingStatus;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "plan_version")
    private Integer planVersion;
}
