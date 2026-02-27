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

import java.util.ArrayList;
import java.util.List;

/**
 * Entity for training_result_history table - History/snapshot of training results
 */
@Entity
@Table(name = "training_result_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"trainingResult", "detailHistories"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingResultHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_result_id")
    @ToString.Exclude
    TrainingResult trainingResult;

    @Column
    Integer version;

    @Column(columnDefinition = "text")
    String title;

    // Snapshot fields
    @Column
    Integer year;

    @Column(name = "team_id")
    Long team_id;

    @Column(name = "line_id")
    Long lineId;

    @Column(name = "status_at_time", length = 50)
    String statusAtTime;

    @Column(columnDefinition = "text")
    String note;


    @OneToMany(mappedBy = "trainingResultHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<TrainingResultDetailHistory> detailHistories = new ArrayList<>();
}
