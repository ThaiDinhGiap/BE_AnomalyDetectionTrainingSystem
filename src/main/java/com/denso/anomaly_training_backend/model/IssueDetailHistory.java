package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.IssueDetailType;
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

@Entity
@Table(name = "issue_detail_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class IssueDetailHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_report_history_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private IssueReportHistory issueReportHistory;

    @Column(name = "defect_description", nullable = false, columnDefinition = "text")
    private String defectDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Process process;

    @Column(name = "detected_date", nullable = false)
    private LocalDate detectedDate;

    @Column(columnDefinition = "text")
    private String note;


    @Enumerated(EnumType.STRING)
    @Column(name = "issue_detail_type")
    IssueDetailType type;

    @Column(name = "target_defect_id")
    Long targetDefectId;
}
