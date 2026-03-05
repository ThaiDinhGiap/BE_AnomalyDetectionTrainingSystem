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
import java.util.Comparator;
import java.util.List;

/**
 * Entity for training_sample_proposals table - Header for training sample proposals
 */
@Entity
@Table(name = "training_sample_proposals")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"productLine", "details"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleProposal extends BaseEntity implements Approvable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    ProductLine productLine;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "current_version")
    @Builder.Default
    Integer currentVersion = 1;

    @Column(name = "form_code", length = 255)
    String formCode;

    @OneToMany(mappedBy = "trainingSampleProposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingSampleProposalDetail> details = new ArrayList<>();

    @Override
    public ApprovalEntityType getEntityType() {
        return ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL;
    }

    @Override
    public Long getGroupId() {
        return productLine.getGroup().getId();
    }

    @Override
    public String computeContentHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|");
        sb.append(currentVersion).append("|");

        details.stream()
                .sorted(Comparator.comparing(TrainingSampleProposalDetail::getId))
                .forEach(spd -> {
                    sb.append(spd.getId()).append(":");
                    sb.append(spd.getTrainingSampleCode()).append(":");
                    sb.append(spd.getProcess()).append(";");
                });

        return DigestUtils.sha256Hex(sb.toString());
    }
}
