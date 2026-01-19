package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.IssueReportStatus;
import com.denso.anomaly_training_backend.enums.RejectLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

@Entity
@Table(name = "issue_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class IssueReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_sv")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User verifiedBySv;

    @Column(name = "verified_at_sv")
    Instant verifiedAtSv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_manager")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User approvedByManager;

    @Column(name = "approved_at_manager")
    Instant approvedAtManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    IssueReportStatus status = IssueReportStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User rejectedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_level")
    RejectLevel rejectLevel;

    @Column(name = "reject_reason", columnDefinition = "text")
    String rejectReason;

    @Column(name = "current_version")
    @Builder.Default
    Integer currentVersion = 1;

    @Column(name = "last_reject_reason", columnDefinition = "text")
    String lastRejectReason;
}
