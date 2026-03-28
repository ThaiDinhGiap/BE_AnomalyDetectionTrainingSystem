package com.sep490.anomaly_training_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sep490.anomaly_training_backend.enums.OAuthProvider;
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
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
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
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50, unique = true)
    String username;

    @Column(name = "password_hash", length = 255)
    String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(length = 100, unique = true)
    String email;

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 20)
    @Builder.Default
    OAuthProvider oauthProvider = OAuthProvider.LOCAL;

    @Column(name = "oauth_provider_id", length = 255)
    String oauthProviderId;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "require_password_change", nullable = false)
    @Builder.Default
    Boolean requirePasswordChange = false;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    Set<Role> roles = new HashSet<>();

    // ================= Security =================

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (roles != null) {
            for (Role r : roles) {

                if (Boolean.TRUE.equals(r.getIsActive())) {

                    authorities.add(new SimpleGrantedAuthority(r.getRoleCode()));

                    if (r.getPermissions() != null) {
                        r.getPermissions().forEach(p ->
                                authorities.add(new SimpleGrantedAuthority(p.getPermissionCode()))
                        );
                    }
                }
            }
        }

        return authorities;
    }

    // ================= Helper =================

    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(r -> r.getRoleCode().equals(roleCode));
    }

    public boolean hasPermission(String permissionCode) {
        if (roles == null) return false;
        return roles.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> permissionCode.equals(p.getPermissionCode()));
    }

    // ================= UserDetails =================

    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive) && !isDeleteFlag();
    }
}