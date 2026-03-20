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

import java.util.ArrayList;
import java.util.List;

/**
 * Entity for training_results table - Header for training results
 */
@Entity
@Table(name = "training_results")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingResult extends BaseEntity implements Approvable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_id")
    @ToString.Exclude
    TrainingPlan trainingPlan;

    @Column(columnDefinition = "text")
    String title;

    @Column(name = "form_code", length = 50)
    String formCode;

    @Column(nullable = false)
    Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    @ToString.Exclude
    ProductLine line;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    ReportStatus status = ReportStatus.ON_GOING;

    @Column(name = "current_version")
    @Builder.Default
    Integer currentVersion = 1;

    @Column(columnDefinition = "text")
    String note;

    @OneToMany(mappedBy = "trainingResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingResultDetail> details = new ArrayList<>();

    @Override
    public ApprovalEntityType getEntityType() {
        return ApprovalEntityType.TRAINING_RESULT;
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
}
