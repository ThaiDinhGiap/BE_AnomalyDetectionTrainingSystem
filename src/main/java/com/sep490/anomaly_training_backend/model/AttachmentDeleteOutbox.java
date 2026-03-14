package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachment_delete_outbox")
@Data
@NoArgsConstructor
public class AttachmentDeleteOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attachment_id")
    private Long attachmentId;

    @Column(name = "bucket")
    private String bucket;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "attempts")
    private int attempts = 0;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt = LocalDateTime.now();

    @Column(name = "last_error")
    private String lastError;



    public AttachmentDeleteOutbox(Long attachmentId, String bucket, String objectKey) {
        this.attachmentId = attachmentId;
        this.bucket = bucket;
        this.objectKey = objectKey;
    }
}
