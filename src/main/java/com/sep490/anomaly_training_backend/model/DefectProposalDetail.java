package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.enums.DefectType;
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

import java.time.LocalDate;

/**
 * Entity for defect_proposal_details table - Detail rows in defect proposals
 */
@Entity
@Table(name = "defect_proposal_details")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DefectProposalDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defect_proposal_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    DefectProposal defectProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Defect defect;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_type", nullable = false)
    @Builder.Default
    ProposalType proposalType = ProposalType.CREATE;

    @Column(name = "defect_description", nullable = false, columnDefinition = "text")
    String defectDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @Column(name = "detected_date", nullable = false)
    LocalDate detectedDate;

    @Column(columnDefinition = "text")
    String note;

    @Column(name = "origin_cause", length = 255)
    String originCause;

    @Column(name = "outflow_cause", length = 255)
    String outflowCause;

    @Column(name = "cause_point", length = 255)
    String causePoint;

    @Column(name = "origin_measures", length = 255)
    String originMeasures;

    @Column(name = "outflow_measures", length = 255)
    String outflowMeasures;

    @Enumerated(EnumType.STRING)
    @Column(name = "defect_type")
    DefectType defectType;

    @Column(name = "customer", length = 255)
    String customer;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "conclusion", columnDefinition = "text")
    String conclusion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Product product;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reject_feedback", columnDefinition = "JSON")
    private RejectFeedbackJson rejectFeedback;
}
