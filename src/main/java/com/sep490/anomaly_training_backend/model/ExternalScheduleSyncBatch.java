package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ExternalScheduleSyncBatchStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Each time your system calls external API to sync schedules, create one batch record.
 */
@Entity
@Table(
        name = "external_schedule_sync_batches",
        indexes = {
                @Index(name = "idx_esb_line_date", columnList = "product_line_id,date_from,date_to"),
                @Index(name = "idx_esb_status", columnList = "status"),
                @Index(name = "idx_esb_delete_flag", columnList = "delete_flag")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ExternalScheduleSyncBatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "source_system", nullable = false, length = 64)
    String sourceSystem;

    @Column(name = "source_endpoint", length = 255)
    String sourceEndpoint;

    @Column(name = "source_version", length = 32)
    String sourceVersion;

    @Column(name = "timezone", nullable = false, length = 64)
    @Builder.Default
    String timezone = "Asia/Ho_Chi_Minh";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @Column(name = "date_from", nullable = false)
    LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    LocalDate dateTo;

    @Column(name = "requested_at")
    LocalDateTime requestedAt;

    @Column(name = "finished_at")
    LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    ExternalScheduleSyncBatchStatus status = ExternalScheduleSyncBatchStatus.RUNNING;

    @Column(name = "total_received", nullable = false)
    @Builder.Default
    Integer totalReceived = 0;

    @Column(name = "total_inserted", nullable = false)
    @Builder.Default
    Integer totalInserted = 0;

    @Column(name = "total_updated", nullable = false)
    @Builder.Default
    Integer totalUpdated = 0;

    @Column(name = "total_failed", nullable = false)
    @Builder.Default
    Integer totalFailed = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;
}