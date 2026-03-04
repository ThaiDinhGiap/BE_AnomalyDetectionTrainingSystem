package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Store raw external schedule record per employee/day for traceability and reprocessing.
 */
@Entity
@Table(
        name = "external_schedule_raw_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_raw_batch_emp_date", columnNames = {"sync_batch_id", "external_employee_id", "work_date"})
        },
        indexes = {
                @Index(name = "idx_raw_batch", columnList = "sync_batch_id"),
                @Index(name = "idx_raw_processed", columnList = "processed")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ExternalScheduleRawRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sync_batch_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ExternalScheduleSyncBatch syncBatch;

    @Column(name = "external_employee_id", nullable = false, length = 64)
    String externalEmployeeId;

    @Column(name = "work_date", nullable = false)
    LocalDate workDate;

    @Column(name = "role_code", length = 50)
    String roleCode;

    @Column(name = "day_type", length = 50)
    String dayType;

    @Column(name = "shift_code", length = 20)
    String shiftCode;

    /**
     * Keep the original external JSON. In DB: JSON type.
     * Here we store as String to match existing repo style (see sample_snapshot columnDefinition = "json").
     */
    @Column(name = "raw_json", nullable = false, columnDefinition = "json")
    String rawJson;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    Boolean processed = false;

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @Column(name = "process_error", columnDefinition = "TEXT")
    String processError;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}