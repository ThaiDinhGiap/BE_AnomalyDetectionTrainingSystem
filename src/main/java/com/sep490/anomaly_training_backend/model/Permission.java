package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"module", "roles"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    String permissionCode;

    @Column(name = "display_name", nullable = false, length = 200)
    String displayName;

    @Column(length = 500)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    @ToString.Exclude
    Module module;

    @Column(nullable = false, length = 50)
    String action;

    @Column(name = "sort_order")
    @Builder.Default
    Integer sortOrder = 0;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    Boolean isSystem = true;

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}
