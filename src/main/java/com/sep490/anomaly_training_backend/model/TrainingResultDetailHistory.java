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

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity for training_result_detail_history table - History/snapshot of training result details
 */
@Entity
@Table(name = "training_result_detail_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingResultDetailHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_history_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingResultHistory trainingResultHistory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_detail_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingResultDetail trainingResultDetail;

    // Snapshot fields
    @Column(name = "training_topic_id")
    Long trainingTopicId;

    @Column(name = "planned_date")
    LocalDate plannedDate;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @Column(name = "product_group_id")
    Long productGroupId;

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

    // Signature snapshots
    @Column(name = "signature_pro_in")
    Long signatureProIn;

    @Column(name = "signature_pro_in_name", length = 100)
    String signatureProInName;

    @Column(name = "signature_fi_in")
    Long signatureFiIn;

    @Column(name = "signature_fi_in_name", length = 100)
    String signatureFiInName;

    @Column(name = "signature_pro_out")
    Long signatureProOut;

    @Column(name = "signature_pro_out_name", length = 100)
    String signatureProOutName;

    @Column(name = "signature_fi_out")
    Long signatureFiOut;

    @Column(name = "signature_fi_out_name", length = 100)
    String signatureFiOutName;
}
