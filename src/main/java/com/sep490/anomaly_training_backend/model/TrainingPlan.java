package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
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
import java.util.List;

/**
 * Entity for training_plan table - Header for training plans
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

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

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

    @Column(name = "min_training_per_day")
    Integer minTrainingPerDay;

    @Column(name = "max_training_per_day")
    Integer maxTrainingPerDay;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingPlanSpecialDay> specialDays = new ArrayList<>();

    // Approval implementation
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
        String sb = id + "|" +
                currentVersion + "|" +
                team.getId() + "|";

        return DigestUtils.sha256Hex(sb);
    }
    @Override
    public String getEntityLabel() {
        return title != null ? title : formCode;
    }

    @Override
    public void clearRejectFeedback() {
        if (!details.isEmpty()) {
            details.forEach(detail -> {detail.setRejectFeedback(null);});
        }
    }
}
