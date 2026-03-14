package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "bucket")
    private String bucket;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "status")
    private String status = "PENDING"; // Default value from DB

    @Column(name = "is_primary")
    private boolean isPrimary = false; // Default value from DB

    @Column(name = "sort_order")
    private int sortOrder = 0; // Default value from DB

    @Column(name = "note")
    private String note;

    @Transient
    private String url;
}
