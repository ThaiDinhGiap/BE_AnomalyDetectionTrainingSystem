package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.ImportStatus;
import com.sep490.anomaly_training_backend.enums.ImportType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Entity for import history table - log transaction while importing
 */
@Entity
@Table(name = "import_histories")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ImportHistory  extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ImportStatus status;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_type")
    private ImportType importType;

    @Column(name = "import_error_description", columnDefinition = "json")
    private String importErrorDescription;
}
