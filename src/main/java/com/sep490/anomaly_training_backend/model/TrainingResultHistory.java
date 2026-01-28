package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for training_result_history table - History/snapshot of training results
 */
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
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_result_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    TrainingResult trainingResult;

    @Column(nullable = false)
    Integer version;

    @Column(columnDefinition = "text")
    String title;

    // Snapshot fields
    @Column(name = "form_code", length = 50)
    String formCode;

    @Column
    Integer year;

    @Column(name = "group_id")
    Long groupId;

    @Column(name = "group_name", length = 100)
    String groupName;

    @Column(columnDefinition = "text")
    String note;

    @Column(name = "recorded_at")
    LocalDateTime recordedAt;

    @OneToMany(mappedBy = "trainingResultHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingResultDetailHistory> detailHistories = new ArrayList<>();
}
