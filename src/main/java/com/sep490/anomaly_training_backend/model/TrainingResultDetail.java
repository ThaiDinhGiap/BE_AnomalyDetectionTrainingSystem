package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.enums.TrainingResultDetailStatus;
import jakarta.persistence.*;
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

/**
 * Entity for training_result_detail table - Detail rows in training results (each test)
 */
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
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingResult trainingResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_detail_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingPlanDetail trainingPlanDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_topic_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingTopic trainingTopic;

    @Column(name = "planned_date", nullable = false)
    LocalDate plannedDate;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_group_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    GroupProduct productGroup;

    @Column(name = "time_in")
    LocalTime timeIn;

    @Column(name = "time_out")
    LocalTime timeOut;

    @Column(name = "training_sample", length = 255)
    String trainingSample;

    @Column(name = "detection_time")
    Integer detectionTime;

    @Column(name = "is_pass")
    Boolean isPass;

    @Column(name = "remedial_action", columnDefinition = "text")
    String remedialAction;

    @Column(columnDefinition = "text")
    String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    TrainingResultDetailStatus status = TrainingResultDetailStatus.PENDING;

    // Signatures
    Long signatureProIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_fi_in")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User signatureFiIn;

    Long signatureProOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_fi_out")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User signatureFiOut;
}