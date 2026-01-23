package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.TrainingPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "training_plan_approval")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlanApproval extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id", nullable = false)
    @ToString.Exclude
    private TrainingPlan trainingPlan;

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
    private TrainingPlanStatus resultingStatus;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "plan_version")
    private Integer planVersion;
}