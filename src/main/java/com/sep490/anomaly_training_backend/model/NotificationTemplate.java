package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "notification_templates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class NotificationTemplate extends BaseEntity {

    @Id
    @Column(length = 50)
    String code;

    @Column(name = "subject_template", nullable = false, length = 300)
    String subjectTemplate;

    @Column(name = "html_template_name", nullable = false, length = 100)
    String htmlTemplateName;

    @Column(length = 500)
    String description;
}