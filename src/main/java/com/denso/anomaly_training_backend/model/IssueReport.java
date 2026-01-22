package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.IssueReportStatus;
import com.denso.anomaly_training_backend.enums.RejectLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue_report")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class IssueReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "verified_by_sv")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private User verifiedBySv;
//
//    @Column(name = "verified_at_sv")
//    private Instant verifiedAtSv;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "approved_by_manager")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private User approvedByManager;
//
//    @Column(name = "approved_at_manager")
//    private Instant approvedAtManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private IssueReportStatus status = IssueReportStatus.DRAFT;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "rejected_by")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private User rejectedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_level")
    private RejectLevel rejectLevel;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @Column(name = "current_version")
    @Builder.Default
    private Integer currentVersion = 1;

    @Column(name = "last_reject_reason", columnDefinition = "text")
    private String lastRejectReason;

    @OneToMany(mappedBy = "issueReport", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<IssueReportApproval> approvalLogs = new ArrayList<>();
}
