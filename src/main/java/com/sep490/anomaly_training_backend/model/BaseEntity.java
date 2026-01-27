package com.sep490.anomaly_training_backend.model;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
@Data
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @Column(name = "delete_flag", nullable = false)
    boolean deleteFlag = false;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    String createdBy;
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    LocalDateTime updatedAt;
    @LastModifiedBy
    @Column(name = "updated_by", insertable = false)
    String updatedBy;
}
