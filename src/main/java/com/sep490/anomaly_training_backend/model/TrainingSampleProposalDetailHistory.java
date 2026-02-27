package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity for training_sample_proposal_detail_history table
 */
@Entity
@Table(name = "training_sample_proposal_detail_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"trainingSampleProposalHistory"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleProposalDetailHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_sample_proposal_history_id")
    @ToString.Exclude
    TrainingSampleProposalHistory trainingSampleProposalHistory;

    // Snapshot fields
    @Column(name = "training_sample_id")
    Long trainingSampleId;

    @Column(name = "proposal_type", length = 20)
    String proposalType;

    @Column(name = "process_id")
    Long processId;

    @Column(name = "process_code", length = 20)
    String processCode;

    @Column(name = "process_name", length = 200)
    String processName;

    @Column(name = "defect_id")
    Long defectId;

    @Column(name = "category_name", length = 200)
    String categoryName;

    @Column(name = "training_sample_code", length = 20)
    String trainingSampleCode;

    @Column(name = "training_description", columnDefinition = "text")
    String trainingDescription;

    @Column(name = "product_id")
    Long productId;

    @Column(columnDefinition = "text")
    String note;
}
