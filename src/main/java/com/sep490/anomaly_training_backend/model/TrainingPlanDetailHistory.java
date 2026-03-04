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

import java.time.LocalDate;

/**
 * Entity for training_plan_detail_history table - History/snapshot of training plan details
 */
@Entity
@Table(name = "training_plan_detail_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlanDetailHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_history_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingPlanHistory trainingPlanHistory;

    // Snapshot fields
    @Column(name = "employee_id")
    Long employeeId;

    @Column(name = "target_month")
    LocalDate targetMonth;

    @Column(name = "planned_date")
    LocalDate plannedDate;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @Column(name = "status", length = 20)
    String status;

    @Column(name = "batch_id", length = 36)
    String batchId;

    @Column(columnDefinition = "text")
    String note;
}
