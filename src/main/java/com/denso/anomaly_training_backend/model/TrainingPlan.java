package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.TrainingPlanStatus;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_plan")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month_start")
    private LocalDate monthStart;

    @Column(name = "month_end")
    private LocalDate monthEnd;

    @Column(name = "form_code", columnDefinition = "text")
    private String code = "TR_PLAN"; // khi lưu trường này xử lý trong service hãy mặc định rằng bắt đầu bằng TR_PLAN_DateTime_Process_Version_UUID(Ngắn) subString)(0, 6)

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Group group;

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
    @Builder.Default
    private TrainingPlanStatus status = TrainingPlanStatus.DRAFT;

    @Column(name = "current_version")
    private Integer currentVersion = 1;

    @Column(name = "last_reject_reason", columnDefinition = "text")
    private String lastRejectReason;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<TrainingPlanDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<TrainingPlanApproval> approvalLogs = new ArrayList<>();

}
