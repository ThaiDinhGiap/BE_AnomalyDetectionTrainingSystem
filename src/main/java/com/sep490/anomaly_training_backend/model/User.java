package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"roles"})
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50, unique = true)
    String username;

    @Column(name = "password_hash", length = 255)
    String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(nullable = false, length = 100, unique = true)
    String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 20)
    @Builder.Default
    OAuthProvider oauthProvider = OAuthProvider.LOCAL;

    @Column(name = "oauth_provider_id", length = 255)
    String oauthProviderId;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}