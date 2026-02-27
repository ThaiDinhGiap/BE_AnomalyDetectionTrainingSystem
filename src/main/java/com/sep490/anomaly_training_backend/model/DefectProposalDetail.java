package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column(name = "is_escaped")
    @Builder.Default
    Boolean isEscaped = false;

    @Column(columnDefinition = "text")
    String note;

    @Column(name = "origin_cause", length = 255)
    String originCause;

    @Column(name = "outflow_cause", length = 255)
    String outflowCause;

    @Column(name = "cause_point", length = 255)
    String causePoint;
}
