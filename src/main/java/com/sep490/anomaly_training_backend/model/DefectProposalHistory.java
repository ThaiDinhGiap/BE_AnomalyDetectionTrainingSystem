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
 * Entity for defect_proposal_history table - History for defect proposal headers
 */
@Entity
@Table(name = "defect_proposal_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"defectProposal", "detailHistory"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DefectProposalHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defect_proposal_id")
    @ToString.Exclude
    DefectProposal defectProposal;

    @Column(nullable = false)
    Integer version;

    // Snapshot fields
    @Column(name = "product_line_id")
    Long productLineId;

    @Column(name = "form_code", length = 255)
    String formCode;

    @Column(name = "recorded_at")
    LocalDateTime recordedAt;

    @OneToMany(mappedBy = "defectProposalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<DefectProposalDetailHistory> detailHistory = new ArrayList<>();
}
