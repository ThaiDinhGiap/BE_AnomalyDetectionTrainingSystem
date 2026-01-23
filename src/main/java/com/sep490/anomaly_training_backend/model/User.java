package com.sep490.anomaly_training_backend.model;

import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
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
}