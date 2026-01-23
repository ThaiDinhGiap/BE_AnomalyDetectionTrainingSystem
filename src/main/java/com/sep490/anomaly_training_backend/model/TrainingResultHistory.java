package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.RejectLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "training_result_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingResultHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingResult trainingResult;

    @Column(name = "title")
    private String title;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "year")
    private Integer year;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "verified_by_sv")
    private Long verifiedBySvId;

    @Column(name = "verified_by_sv_name")
    private String verifiedBySvName;

    @Column(name = "reject_by")
    private Long rejectedById;

    @Column(name = "reject_by_name")
    private String rejectedByName;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_level")
    private RejectLevel rejectLevel;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    // Quan hệ để lưu danh sách detail history
    @OneToMany(mappedBy = "trainingResultHistory", cascade = CascadeType.ALL)
    List<TrainingResultDetailHistory> historyDetails;
}
