package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for training_plan_history table - History/snapshot of training plans
 */
@Entity
@Table(name = "training_plan_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"trainingPlan", "detailHistory"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlanHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "text")
    String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id")
    @ToString.Exclude
    TrainingPlan trainingPlan;

    @Column(nullable = false)
    Integer version;

    // Snapshot fields
    @Column(name = "form_code", length = 50)
    String formCode;

    @Column(name = "month_start")
    LocalDate monthStart;

    @Column(name = "month_end")
    LocalDate monthEnd;

    @Column(name = "team_id")
    Long team_id;

    @Column(name = "line_id")
    Long lineId;

    @Column(columnDefinition = "text")
    String note;

    @Column(name = "recorded_at")
    LocalDateTime recordedAt;

    @OneToMany(mappedBy = "trainingPlanHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingPlanDetailHistory> detailHistories = new ArrayList<>();
}
