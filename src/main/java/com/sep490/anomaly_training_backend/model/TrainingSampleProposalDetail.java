package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.enums.ProposalType;
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

/**
 * Entity for training_sample_proposal_details table - Detail rows in training sample proposals
 */
@Entity
@Table(name = "training_sample_proposal_details")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleProposalDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_sample_proposal_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingSampleProposal trainingSampleProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_sample_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingSample trainingSample;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_type", nullable = false)
    ProposalType proposalType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Defect defect;

    @Column(name = "category_name", nullable = false, length = 200)
    String categoryName;

    @Column(name = "training_sample_code", length = 20)
    String trainingSampleCode;

    @Column(name = "training_description", nullable = false, columnDefinition = "text")
    String trainingDescription;

    @Column(columnDefinition = "text")
    String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reject_feedback", columnDefinition = "JSON")
    private RejectFeedbackJson rejectFeedback;
}
