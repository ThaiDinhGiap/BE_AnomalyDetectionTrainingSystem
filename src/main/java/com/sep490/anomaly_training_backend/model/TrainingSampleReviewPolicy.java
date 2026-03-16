package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "training_sample_review_policies")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSampleReviewPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "policy_code", length = 50, unique = true, nullable = false)
    String policyCode;

    @Column(name = "effective_date", nullable = false)
    LocalDate effectiveDate;

    @Column(name = "expiration_date")
    LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    PolicyStatus status = PolicyStatus.ACTIVE;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;
}

