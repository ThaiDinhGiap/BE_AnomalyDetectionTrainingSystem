package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ReportType;
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

/**
 * Entity for training_topic_report_detail table - Detail rows in training topic reports
 */
@Entity
@Table(name = "training_topic_report_detail")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingTopicReportDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_topic_report_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingTopicReport trainingTopicReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_topic_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingTopic trainingTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    @Builder.Default
    ReportType reportType = ReportType.CREATE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Defect defect;

    @Column(name = "category_name", nullable = false, length = 200)
    String categoryName;

    @Column(name = "training_sample", columnDefinition = "text")
    String trainingSample;

    @Column(name = "training_detail", nullable = false, columnDefinition = "text")
    String trainingDetail;

    @Column(columnDefinition = "text")
    String note;
}
