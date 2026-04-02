package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity for training_result_details table - Detail rows in training results
 * (each test)
 */
@Entity
@Table(name = "training_result_details")
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "training_plan_detail_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingPlanDetail trainingPlanDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_sample_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingSample trainingSample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Product product;

    @Column
    Integer classification;

    @Column(name = "training_topic", length = 255)
    String trainingTopic;

    @Column(name = "sample_code", length = 20)
    String sampleCode;

    @Column(name = "cycle_time_standard")
    BigDecimal cycleTimeStandard;

    @Column(name = "planned_date", nullable = false)
    LocalDate plannedDate;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @Column(name = "time_in")
    LocalTime timeIn;

    @Column(name = "time_start_op")
    LocalTime timeStartOp;

    @Column(name = "time_out")
    LocalTime timeOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    ReportStatus status = ReportStatus.PENDING_REVIEW;

    @Column(name = "detection_time")
    Integer detectionTime;

    @Column(name = "is_pass")
    Boolean isPass;

    @Column(columnDefinition = "text")
    String note;

    @Column(name = "is_retrained")
    Boolean isRetrained;

    // Signatures
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_pro_in")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User signatureProIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_fi_in")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User signatureFiIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_pro_out")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User signatureProOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_fi_out")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User signatureFiOut;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reject_feedback", columnDefinition = "JSON")
    private RejectFeedbackJson rejectFeedback;

    /**
     * Mã nhóm batch từ TrainingPlanDetail, dùng để group các result details
     * của cùng 1 employee trên FE.
     */
    @Column(name = "batch_id", length = 36)
    String batchId;

    public String computeContentHash() {
        String sb = id + "|" +
                trainingResult.getCurrentVersion() + "|" +
                trainingResult.getTeam().getId() + "|";

        return DigestUtils.sha256Hex(sb);
    }

}
