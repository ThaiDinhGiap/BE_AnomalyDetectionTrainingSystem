package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.RejectLevel;
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
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "training_plan_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlanHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingPlan trainingPlan;

    @Column(name = "title")
    private String title;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "month_start")
    private LocalDate monthStart;

    @Column(name = "month_end")
    private LocalDate monthEnd;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "verified_by_sv")
    private Long verifiedBySvId;

    @Column(name = "verified_by_sv_name")
    private String verifiedBySvName;

    @Column(name = "reject_by")
    private Long rejectedById;

    @Column(name = "reject_by_name")
    private String rejectedByName;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_level")
    private RejectLevel rejectLevel;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @OneToMany(mappedBy = "trainingPlanHistory", cascade = CascadeType.ALL)
    List<TrainingPlanDetailHistory> historyDetails;
}
