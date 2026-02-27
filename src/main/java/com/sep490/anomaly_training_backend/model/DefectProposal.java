package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ProposalStatus;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Entity for defect_proposals table - Header for defect proposals
 */
@Entity
@Table(name = "defect_proposals")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"productLine", "details"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DefectProposal extends BaseEntity {
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
    ProposalStatus status = ProposalStatus.DRAFT;

    @Column(name = "current_version")
    @Builder.Default
    Integer currentVersion = 1;

    @Column(name = "form_code", length = 255)
    String formCode;

    @OneToMany(mappedBy = "defectProposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<DefectProposalDetail> details = new ArrayList<>();
}
