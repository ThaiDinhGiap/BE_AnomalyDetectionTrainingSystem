package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Map external system employeeId -> internal Employee (employees.id)
 * Used during sync to join external schedules with internal employees table.
 */
@Entity
@Table(
        name = "external_employee_mappings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_external_employee_mapping", columnNames = {"source_system", "external_employee_id"})
        },
        indexes = {
                @Index(name = "idx_external_employee_mappings_employee", columnList = "employee_id"),
                @Index(name = "idx_external_employee_mappings_delete_flag", columnList = "delete_flag")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ExternalEmployeeMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "source_system", nullable = false, length = 64)
    String sourceSystem;

    @Column(name = "external_employee_id", nullable = false, length = 64)
    String externalEmployeeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Employee employee;

    @Column(name = "active", nullable = false)
    @Builder.Default
    Boolean active = true;
}