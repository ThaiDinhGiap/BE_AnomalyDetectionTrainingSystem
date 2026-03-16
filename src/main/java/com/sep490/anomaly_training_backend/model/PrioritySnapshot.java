package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

import java.util.List;

/**
 * Entity for priority_snapshots table
 * <p>
 * Lưu trữ snapshot của 1 priority policy áp dụng cho 1 team tại 1 thời điểm
 * <p>
 * VD: Policy "Training Priority 2027" → Team "Production Line A" tại 2026-03-14
 * Kết quả: 150 employees được phân loại vào các tiers
 */
@Entity
@Table(
        name = "priority_snapshots",
        indexes = {
                @Index(name = "idx_ps_team_policy", columnList = "team_id,policy_id"),
                @Index(name = "idx_ps_generated_at", columnList = "generated_at"),
                @Index(name = "idx_ps_delete_flag", columnList = "delete_flag")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PrioritySnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * Reference tới Team
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Team team;

    /**
     * Reference tới Priority Policy
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    PriorityPolicy policy;

    /**
     * Snapshot của policy definition tại thời điểm generate (JSON)
     * Để track các thay đổi của policy từ lần snapshot trước đó
     * <p>
     * Format JSON: {
     * "policyCode": "TRAIN_2027",
     * "policyName": "Training Priority 2027",
     * "tiers": [
     * {
     * "tierOrder": 1,
     * "tierName": "Critical",
     * "filters": [...],
     * "rankingMetric": "days_since_last_training"
     * },
     * ...
     * ]
     * }
     */
    @Column(name = "policy_snapshot", columnDefinition = "JSON", nullable = false)
    String policySnapshot;

    /**
     * Optional: Link tới training plan (nếu snapshot được generate từ training plan)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingPlan trainingPlan;

    /**
     * One-to-Many: Snapshot có nhiều details (1 detail = 1 employee)
     */
    @OneToMany(mappedBy = "snapshot", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<PrioritySnapshotDetail> details;
}