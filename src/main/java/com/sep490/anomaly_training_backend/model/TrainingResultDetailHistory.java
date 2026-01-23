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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_detail_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingResultDetail trainingResultDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_history_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingResultHistory trainingResultHistory;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_group_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GroupProduct productGroup;

    @Column(name = "time_in")
    private LocalTime timeIn;

    @Column(name = "time_out")
    private LocalTime timeOut;

    @Column(name = "signature_pro_in")
    private Long signatureProInId;

    @Column(name = "signature_pro_in_name")
    private String signatureProInName;

    @Column(name = "signature_fi_in")
    private Long signatureFiInId;

    @Column(name = "signature_fi_in_name")
    private String signatureFiInName;

    @Column(name = "signature_pro_out")
    private Long signatureProOutId;

    @Column(name = "signature_pro_out_name")
    private String signatureProOutName;

    @Column(name = "signature_fi_out")
    private Long signatureFiOutId;

    @Column(name = "signature_fi_out_name")
    private String signatureFiOutName;

    @Column(name = "detection_time")
    private Integer detectionTime;

    @Column(name = "is_pass")
    private Boolean isPass;

    @Column(name = "remedial_action", columnDefinition = "text")
    private String remedialAction;

    @Column(name = "reject_by")
    private Long rejectedById;

    @Column(name = "reject_by_name")
    private String rejectedByName;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;
}
