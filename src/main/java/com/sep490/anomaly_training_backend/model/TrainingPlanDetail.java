package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
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

import java.time.LocalDate;

/**
 * Entity for training_plan_detail table - Detail rows in training plans
 */
@Entity
@Table(name = "training_plan_detail")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlanDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingPlan trainingPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @Column(name = "target_month")
    LocalDate targetMonth;

    @Column(name = "planned_date")
    LocalDate plannedDate;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    ReportStatus status = ReportStatus.PENDING;

    @Column(columnDefinition = "text")
    String note;
}
