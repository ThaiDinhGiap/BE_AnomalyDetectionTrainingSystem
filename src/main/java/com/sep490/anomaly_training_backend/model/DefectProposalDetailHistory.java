package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Entity for defect_proposal_detail_history table - History for defect proposal details
 */
@Entity
@Table(name = "defect_proposal_detail_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DefectProposalDetailHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defect_proposal_history_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    DefectProposalHistory defectProposalHistory;

    // Snapshot fields
    @Column(name = "defect_id")
    Long defectId;

    @Column(name = "proposal_type", length = 20)
    String proposalType;

    @Column(name = "defect_description", columnDefinition = "text")
    String defectDescription;

    @Column(name = "process_id")
    Long processId;

    @Column(name = "process_code", length = 20)
    String processCode;

    @Column(name = "process_name", length = 200)
    String processName;

    @Column(name = "detected_date")
    LocalDate detectedDate;

    @Column(name = "is_escaped")
    Boolean isEscaped;

    @Column(columnDefinition = "text")
    String note;

    @Column(name = "origin_cause", length = 255)
    String originCause;

    @Column(name = "outflow_cause", length = 255)
    String outflowCause;

    @Column(name = "cause_point", length = 255)
    String causePoint;
}
