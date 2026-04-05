package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity for training_samples table - Master data for approved training samples
 */
@Entity
@Table(name = "training_samples", uniqueConstraints = {
        @UniqueConstraint(name = "uk_training_samples_code", columnNames = {"product_line_id", "sample_code"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrainingSample extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "training_code", nullable = false, unique = true, length = 20)
    String trainingCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_line_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    ProductLine productLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Defect defect;

    @Column(name = "category_name", nullable = false, length = 200)
    String categoryName;

    @Column(name = "training_description", nullable = false, columnDefinition = "text")
    String trainingDescription;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "training_sample_products",
        joinColumns = @JoinColumn(name = "training_sample_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    Set<Product> products = new HashSet<>();

    @Column(name = "training_sample_code", length = 20)
    String trainingSampleCode;

    @Column(name = "process_order", nullable = false)
    Integer processOrder;

    @Column(name = "category_order", nullable = false)
    Integer categoryOrder;

    @Column(name = "content_order", nullable = false)
    Integer contentOrder;

    @Column(columnDefinition = "text")
    String note;
}
