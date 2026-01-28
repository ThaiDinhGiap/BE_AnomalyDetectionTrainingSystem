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

import java.time.LocalDate;

/**
 * Entity for defect_report_detail table - Detail rows in defect reports
 */
@Entity
@Table(name = "defect_report_detail")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DefectReportDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defect_report_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    DefectReport defectReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Defect defect;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    @Builder.Default
    ReportType reportType = ReportType.CREATE;

    @Column(name = "defect_description", nullable = false, columnDefinition = "text")
    String defectDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @Column(name = "detected_date", nullable = false)
    LocalDate detectedDate;

    @Column(columnDefinition = "text")
    String note;
}
