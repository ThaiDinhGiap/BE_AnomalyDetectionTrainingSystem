package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for training_sample_proposal_history table
 */
@Entity
@Table(name = "training_sample_proposal_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"trainingSampleProposal", "detailHistory"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleProposalHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_sample_proposal_id")
    @ToString.Exclude
    TrainingSampleProposal trainingSampleProposal;

    @Column(nullable = false)
    Integer version;

    // Snapshot fields
    @Column(name = "product_line_id")
    Long productLineId;

    @Column(name = "recorded_at")
    LocalDateTime recordedAt;

    @OneToMany(mappedBy = "trainingSampleProposalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingSampleProposalDetailHistory> detailHistory = new ArrayList<>();
}
