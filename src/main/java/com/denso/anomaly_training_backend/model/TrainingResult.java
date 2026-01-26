package com.denso.anomaly_training_backend.model;

import com.denso.anomaly_training_backend.enums.TrainingResultStatus;
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
@Table(name = "training_result")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_code", columnDefinition = "text")
    private String code = "TR_RESULT"; // khi lưu trường này xử lý trong service hãy mặc định rằng bắt đầu bằng TR_RESULT_DateTime_Group_Version_01 tăng dần

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "year", nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Group group;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "confirm_by_fi")
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    private User confirmByFi;
//
//    @Column(name = "confirm_at_fi")
//    private Instant confirmAtFi;
//
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
    private TrainingResultStatus status = TrainingResultStatus.ON_GOING;

    @Column(name = "current_version")
    private Integer currentVersion = 1;

    @Column(name = "last_reject_reason", columnDefinition = "text")
    private String lastRejectReason;

    @OneToMany(mappedBy = "trainingResult", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<TrainingResultApproval> approvalLogs = new ArrayList<>();
}
