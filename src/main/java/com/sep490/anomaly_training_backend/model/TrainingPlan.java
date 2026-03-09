package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import jakarta.persistence.CascadeType;
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
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate Root for the TrainingPlan aggregate.
 * <p>
 * This entity controls all access to its child {@link TrainingPlanDetail} entities.
 * All modifications to details must go through methods on this aggregate root
 * to enforce business invariants (status guards, date range, duplicate checks).
 * </p>
 */
@Entity
@Table(name = "training_plans")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlan extends BaseEntity implements Approvable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "text")
    String title;

    @Column(name = "form_code", length = 50)
    @Builder.Default
    String formCode = "TR_PLAN";

    @Column(name = "month_start")
    LocalDate monthStart;

    @Column(name = "month_end")
    LocalDate monthEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine line;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "current_version")
    @Builder.Default
    Integer currentVersion = 1;

    @Column(columnDefinition = "text")
    String note;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingPlanDetail> details = new ArrayList<>();

    // ==================== AGGREGATE ROOT — STATUS GUARDS ====================

    /**
     * Check if this plan can be edited (add/update/delete details).
     * Plans waiting for approval cannot be modified.
     */
    public boolean isEditable() {
        return status != ReportStatus.WAITING_SV && status != ReportStatus.WAITING_MANAGER;
    }

    /**
     * Check if this plan can be deleted.
     * Only DRAFT or REJECTED plans can be permanently deleted.
     */
    public boolean isDeletable() {
        return status == ReportStatus.DRAFT
                || status == ReportStatus.REJECTED_BY_MANAGER
                || status == ReportStatus.REJECTED_BY_SV;
    }

    /**
     * Check if this plan has been approved.
     */
    public boolean isApproved() {
        return ReportStatus.APPROVED.equals(status);
    }

    // ==================== AGGREGATE ROOT — DETAIL MANAGEMENT ====================

    /**
     * Returns an unmodifiable view of the details for read-only access.
     * Use aggregate root methods (addDetail, removeDetail) to modify the collection.
     */
    public List<TrainingPlanDetail> getDetailsView() {
        return Collections.unmodifiableList(details);
    }

    /**
     * Add a detail to this plan. Sets the back-reference automatically.
     *
     * @param detail the detail to add (must have employee and plannedDate set)
     * @throws IllegalStateException if the plan is not editable
     */
    public void addDetail(TrainingPlanDetail detail) {
        guardEditable();
        detail.setTrainingPlan(this);
        this.details.add(detail);
    }

    /**
     * Find a detail by its ID within this aggregate.
     *
     * @param detailId the detail ID
     * @return Optional containing the detail, or empty if not found
     */
    public Optional<TrainingPlanDetail> findDetailById(Long detailId) {
        return details.stream()
                .filter(d -> d.getId() != null && d.getId().equals(detailId))
                .findFirst();
    }

    /**
     * Find all details belonging to a specific batch.
     *
     * @param batchId the batch identifier
     * @return list of details in that batch
     */
    public List<TrainingPlanDetail> findDetailsByBatchId(String batchId) {
        return details.stream()
                .filter(d -> batchId.equals(d.getBatchId()))
                .toList();
    }

    /**
     * Remove a detail from a DRAFT/REJECTED plan (physical delete via orphanRemoval).
     * Caller must handle FK cleanup (e.g., TrainingResultDetail) before calling this.
     *
     * @param detail the detail to remove
     * @throws IllegalStateException if the plan is not editable or not in a deletable state
     */
    public void removeDetail(TrainingPlanDetail detail) {
        guardEditable();
        this.details.remove(detail);
    }

    /**
     * Handle detail removal based on plan status:
     * - DRAFT/REJECTED: physically removes the detail (orphanRemoval)
     * - APPROVED: marks the detail as MISSED instead of deleting
     *
     * @param detail the detail to remove or mark as missed
     * @throws IllegalStateException if the plan is not editable
     * @throws IllegalStateException if trying to remove a DONE detail from an approved plan
     */
    public void removeOrMarkMissed(TrainingPlanDetail detail) {
        guardEditable();
        if (isApproved()) {
            if (detail.getStatus() == TrainingPlanDetailStatus.DONE) {
                throw new IllegalStateException(
                        "Không thể xóa detail đã hoàn thành (ID: " + detail.getId() + ")");
            }
            detail.setStatus(TrainingPlanDetailStatus.MISSED);
            detail.setNote("[Đã hủy] " + (detail.getNote() != null ? detail.getNote() : ""));
        } else {
            this.details.remove(detail);
        }
    }

    // ==================== AGGREGATE ROOT — HEADER UPDATE ====================

    /**
     * Update header fields. Only non-null values are applied.
     *
     * @throws IllegalStateException if the plan is not editable
     * @throws IllegalArgumentException if monthEnd is before monthStart
     */
    public void updateHeader(String title, String note, LocalDate monthStart, LocalDate monthEnd) {
        guardEditable();

        if (title != null) {
            this.title = title;
        }
        if (note != null) {
            this.note = note;
        }
        if (monthStart != null) {
            this.monthStart = monthStart;
        }
        if (monthEnd != null) {
            this.monthEnd = monthEnd;
        }

        // Validate date range after update
        LocalDate effectiveStart = this.monthStart;
        LocalDate effectiveEnd = this.monthEnd;
        if (effectiveStart != null && effectiveEnd != null && effectiveEnd.isBefore(effectiveStart)) {
            throw new IllegalArgumentException("Tháng kết thúc không được nhỏ hơn tháng bắt đầu");
        }
    }

    // ==================== AGGREGATE ROOT — BUSINESS VALIDATION ====================

    /**
     * Validate all business invariants before submission for approval.
     *
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public void validateForSubmission() {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Plan dont have details." +
                    "Please enter 1 detail line at least before submit.");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Plan's title cant be empty.");
        }

        if (monthEnd.isBefore(monthStart)) {
            throw new IllegalArgumentException("Month End can be before Month Start.");
        }

        for (TrainingPlanDetail detail : details) {
            if (detail.getEmployee() == null) {
                throw new IllegalArgumentException("Detail lack of Employee Information.");
            }
            if (detail.getPlannedDate() == null) {
                throw new IllegalArgumentException("Detail lack of Planned Date.");
            }

            // Validate plannedDate is within plan's date range
            if (detail.getPlannedDate().isBefore(monthStart) ||
                detail.getPlannedDate().isAfter(monthEnd)) {
                throw new IllegalArgumentException(
                    String.format("Ngày huấn luyện %s nằm ngoài khoảng thời gian kế hoạch (%s - %s)",
                        detail.getPlannedDate(), monthStart, monthEnd)
                );
            }
        }

        // Check for duplicates: same employee + date + batchId
        for (int i = 0; i < details.size(); i++) {
            TrainingPlanDetail detail1 = details.get(i);
            for (int j = i + 1; j < details.size(); j++) {
                TrainingPlanDetail detail2 = details.get(j);

                boolean sameEmployee = detail1.getEmployee().getId().equals(detail2.getEmployee().getId());
                boolean sameDate = detail1.getPlannedDate().equals(detail2.getPlannedDate());
                boolean sameBatch = (detail1.getBatchId() != null && detail1.getBatchId().equals(detail2.getBatchId()));

                if (sameEmployee && sameDate && sameBatch) {
                    throw new IllegalArgumentException(
                        String.format("Trùng lặp: Nhân viên '%s' đã được lên lịch huấn luyện vào ngày %s trong cùng 1 lần thêm",
                            detail1.getEmployee().getFullName(),
                            detail1.getPlannedDate())
                    );
                }
            }
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private void guardEditable() {
        if (!isEditable()) {
            throw new IllegalStateException("Không thể chỉnh sửa khi kế hoạch đang chờ duyệt.");
        }
    }

    // ==================== APPROVAL INTERFACE IMPLEMENTATION ====================

    @Override
    public ApprovalEntityType getEntityType() {
        return ApprovalEntityType.TRAINING_PLAN;
    }

    @Override
    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    @Override
    public Long getGroupId() {
        return team.getGroup().getId();
    }

    @Override
    public String computeContentHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|");
        sb.append(currentVersion).append("|");
        sb.append(team.getId()).append("|");

        details.stream()
                .sorted(Comparator.comparing(TrainingPlanDetail::getId))
                .forEach(tld -> {
                    sb.append(tld.getId()).append(":");
                    sb.append(tld.getEmployee().getEmployeeCode()).append(":");
//                    sb.append(tld.getProcess().getName()).append(":");
                    sb.append(tld.getPlannedDate()).append(";");
                });

        return DigestUtils.sha256Hex(sb.toString());
    }
}
