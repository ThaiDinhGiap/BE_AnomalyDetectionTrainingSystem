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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Entity for priority_snapshot_details table
 * <p>
 * Lưu trữ chi tiết ưu tiên của 1 employee trong 1 snapshot
 * <p>
 * VD: Employee "John Doe" (EMP001) trong snapshot "Policy 2027 - Line A"
 * → Tier Order: 1 (Critical)
 * → Sort Rank: 3 (rank thứ 3 trong tier)
 * → Metrics: {days_since_last_training: 90, fail_rate: 45.5, ...}
 */
@Entity
@Table(
        name = "priority_snapshot_details",
        indexes = {
                @Index(name = "idx_psd_snapshot", columnList = "snapshot_id"),
                @Index(name = "idx_psd_employee", columnList = "employee_id"),
                @Index(name = "idx_psd_rank", columnList = "snapshot_id,sort_rank"),
                @Index(name = "idx_psd_tier", columnList = "snapshot_id,tier_order"),
                @Index(name = "idx_psd_delete_flag", columnList = "delete_flag")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PrioritySnapshotDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * Reference tới Priority Snapshot (parent)
     * CASCADE DELETE: Xoá snapshot → xoá tất cả details
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    PrioritySnapshot snapshot;

    /**
     * Reference tới Employee
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Employee employee;

    /**
     * Snapshot của employee code tại thời điểm generate
     * Để track trong case employee code bị change
     */
    @Column(name = "employee_code", length = 20, nullable = false)
    String employeeCode;

    /**
     * Snapshot của employee full name tại thời điểm generate
     */
    @Column(name = "full_name", length = 100, nullable = false)
    String fullName;

    /**
     * Tier order (level ưu tiên)
     * VD: 1 = Tier 1 (Critical), 2 = Tier 2 (At Risk), etc.
     */
    @Column(name = "tier_order", nullable = false)
    Integer tierOrder;

    /**
     * Tier name (display)
     * VD: "Critical Cases", "At Risk", "Monitor"
     */
    @Column(name = "tier_name", length = 100)
    String tierName;

    /**
     * Rank của employee trong tier (1-based)
     * VD: Tier 1 có 50 employees → sort_rank từ 1 đến 50
     * Tier 2 có 100 employees → sort_rank từ 1 đến 100
     * <p>
     * Sort rank = thứ tự ưu tiên trong tier
     * Rank thấp hơn = ưu tiên cao hơn
     */
    @Column(name = "sort_rank", nullable = false)
    Integer sortRank;

    /**
     * Priority tags (JSON)
     * Lưu thêm các tags/labels để phân loại
     * <p>
     * Format JSON: {
     * "riskLevel": "HIGH",
     * "trainingStatus": "OVERDUE",
     * "skillGap": "CRITICAL"
     * }
     * <p>
     * Optional: có thể NULL
     */
    @Column(name = "priority_tags", columnDefinition = "JSON")
    String priorityTags;

    /**
     * Metric values (JSON)
     * Lưu tất cả metric values được tính toán cho employee này
     * <p>
     * Format JSON: {
     * "days_since_last_training": 90,
     * "fail_rate": 45.5,
     * "years_of_service": 5,
     * "is_on_watchlist": false
     * }
     * <p>
     * Dùng để:
     * - Hiển thị lên UI
     * - Audit/track lịch sử
     * - Debug/troubleshoot nếu cần recalculate
     */
    @Column(name = "metric_values", columnDefinition = "JSON", nullable = false)
    String metricValues;
}