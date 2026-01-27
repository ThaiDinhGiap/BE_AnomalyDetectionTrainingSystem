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
 * Entity for training_topic_report_detail_history table - History/snapshot of training topic report details
 */
@Entity
@Table(name = "training_topic_report_detail_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingTopicReportDetailHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_topic_report_history_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingTopicReportHistory trainingTopicReportHistory;

    // Snapshot fields
    @Column(name = "training_topic_id")
    Long trainingTopicId;

    @Column(name = "report_type", length = 20)
    String reportType;

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

    @Column(name = "training_sample", columnDefinition = "text")
    String trainingSample;

    @Column(name = "training_detail", columnDefinition = "text")
    String trainingDetail;

    @Column(columnDefinition = "text")
    String note;
}
