package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

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

    @Column(name = "policy_name", length = 100, nullable = false)
    String policyName;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @OneToMany(mappedBy = "reviewPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    List<TrainingSampleReviewConfig> reviewConfigs;
}

