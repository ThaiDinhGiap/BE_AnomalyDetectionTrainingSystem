package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"permissions", "users"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    String roleCode;

    @Column(name = "display_name", length = 100)
    String displayName;

    @Column(length = 500)
    String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    Boolean isSystem = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @ToString.Exclude
    @Builder.Default
    Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    @Builder.Default
    Set<User> users = new HashSet<>();
}
