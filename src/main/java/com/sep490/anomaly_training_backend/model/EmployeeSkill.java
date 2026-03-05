package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Entity for employee_skills table - Employee skill certifications
 */
@Entity
@Table(name = "employee_skills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_process", columnNames = {"employee_id", "process_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EmployeeSkill extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Process process;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    EmployeeSkillStatus status = EmployeeSkillStatus.VALID;

    @Column(name = "certified_date")
    LocalDate certifiedDate;

    @Column(name = "expiry_date")
    LocalDate expiryDate;
}
