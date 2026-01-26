package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.TrainingResultDetailStatus;
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

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "training_result_detail")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingResultDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- REFACTORED RELATIONSHIPS ---

    // 1. Training Result (Parent)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_result_id", nullable = false)
    @ToString.Exclude // Prevent infinite loop / lazy initialization error
    @EqualsAndHashCode.Exclude
    private TrainingResult trainingResult;

    // 2. The specific field you asked about
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_plan_detail_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingPlanDetail trainingPlanDetail;

    // 3. Product Group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_group_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GroupProduct groupProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_training_content_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DefectTrainingContent defectTrainingContent;

    // --- SIMPLE COLUMNS ---

    @Column(name = "training_sample", columnDefinition = "text")
    private String trainingSample;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "time_in")
    private LocalTime timeIn;

    @Column(name = "time_out")
    private LocalTime timeOut;

    // --- USER SIGNATURES (Should also be Objects) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_pro_in")
    @ToString.Exclude
    private User signatureProIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_fi_in")
    @ToString.Exclude
    private User signatureFiIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_pro_out")
    @ToString.Exclude
    private User signatureProOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_fi_out")
    @ToString.Exclude
    private User signatureFiOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sv_confirmation")
    @ToString.Exclude
    private User svConfirmation;

    // --- OTHER FIELDS ---

    @Column(name = "detection_time")
    private Integer detectionTime;

    @Column(name = "is_pass")
    private Boolean isPass;

    @Column(name = "remedial_action", columnDefinition = "text")
    private String remedialAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private TrainingResultDetailStatus status = TrainingResultDetailStatus.DRAFT;

    @Column(name = "current_version")
    @Builder.Default
    private Integer currentVersion = 1;

    @Column(name = "last_reject_reason", columnDefinition = "text")
    private String lastRejectReason;
}