package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        Role r = new Role();
        r.setRoleCode("ROLE_USER");
        
        user = User.builder()
                .id(1L)
                .username("usr1")
                .passwordHash("pass")
                .isActive(true)
                .roles(Set.of(r))
                .build();
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsernameWithRolesAndPermissions("usr1")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("usr1");

        assertThat(details.getUsername()).isEqualTo("usr1");
        assertThat(details.getPassword()).isEqualTo("pass");
        assertThat(details.getAuthorities()).hasSize(1);
    }

    @Test
    void loadUserByUsername_NotFound_ShouldThrow() {
        when(userRepository.findByUsernameWithRolesAndPermissions("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
