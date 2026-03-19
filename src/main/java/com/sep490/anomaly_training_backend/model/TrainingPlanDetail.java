package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

/**
 * Entity for training_plan_details table - Detail rows in training plans
 */
@Entity
@Table(name = "training_plan_details")
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

    @Column(name = "target_month")
    LocalDate targetMonth;

    @Column(name = "planned_date")
    LocalDate plannedDate;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    TrainingPlanDetailStatus status = TrainingPlanDetailStatus.PENDING;

    /**
     * Mã nhóm để phân biệt các lần thêm khác nhau của cùng 1 employee.
     * VD: Employee A thêm lần 1 → batchId = "abc123", lần 2 → batchId = "def456"
     */
    @Column(name = "batch_id", length = 36)
    String batchId;

    @Column(columnDefinition = "text")
    String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reject_feedback", columnDefinition = "JSON")
    private RejectFeedbackJson rejectFeedback;
}
