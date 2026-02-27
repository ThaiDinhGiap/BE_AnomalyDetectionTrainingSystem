package com.sep490.anomaly_training_backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"permissions"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Module extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "module_code", nullable = false, unique = true, length = 50)
    String moduleCode;

    @Column(name = "display_name", nullable = false, length = 200)
    String displayName;

    @Column(length = 500)
    String description;

    @Column(name = "sort_order")
    @Builder.Default
    Integer sortOrder = 0;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    List<Permission> permissions = new ArrayList<>();
}
