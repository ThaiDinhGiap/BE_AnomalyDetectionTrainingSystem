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

import java.math.BigDecimal;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_result_history_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingResultHistory trainingResultHistory;

    @Column(name = "training_result_detail_id")
    Long trainingResultDetailId;

    // Snapshot IDs
    @Column(name = "employee_id")
    Long employeeId;

    @Column(name = "process_id")
    Long processId;

    @Column(name = "training_sample_id")
    Long trainingSampleId;

    @Column(name = "product_id")
    Long productId;

    // Snapshot test data
    @Column
    Integer classification;

    @Column(name = "cycle_time_standard")
    BigDecimal cycleTimeStandard;

    @Column(name = "actual_date")
    LocalDate actualDate;

    @Column(name = "time_in")
    LocalTime timeIn;

    @Column(name = "time_start_op")
    LocalTime timeStartOp;

    @Column(name = "time_out")
    LocalTime timeOut;

    @Column(name = "detection_time")
    Integer detectionTime;

    @Column(name = "is_pass")
    Boolean isPass;

    // Signature name snapshots
    @Column(name = "signature_pro_in_name", length = 100)
    String signatureProInName;

    @Column(name = "signature_fi_in_name", length = 100)
    String signatureFiInName;

    @Column(name = "signature_pro_out_name", length = 100)
    String signatureProOutName;

    @Column(name = "signature_fi_out_name", length = 100)
    String signatureFiOutName;
}
