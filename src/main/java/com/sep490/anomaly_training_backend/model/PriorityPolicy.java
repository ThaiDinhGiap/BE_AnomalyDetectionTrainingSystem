package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "priority_policies")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"tiers"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PriorityPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "policy_code", length = 50, unique = true, nullable = false)
    String policyCode;

    @Column(name = "policy_name", length = 100, nullable = false)
    String policyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    PolicyEntityType entityType;

    @Column(name = "effective_date", nullable = false)
    LocalDate effectiveDate;

    @Column(name = "expiration_date")
    LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    PolicyStatus status = PolicyStatus.DRAFT;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    List<PriorityTier> tiers = new ArrayList<>();
}
