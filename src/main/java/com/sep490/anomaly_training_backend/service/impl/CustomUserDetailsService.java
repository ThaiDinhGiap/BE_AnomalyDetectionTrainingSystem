package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security. core.authority.SimpleGrantedAuthority;
import org.springframework. security.core.userdetails. UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype. Service;

import java.util. Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndDeleteFlagFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:  " + username));

        if (! user.getIsActive()) {
            throw new UsernameNotFoundException("User is inactive: " + username);
        }

        return new org.springframework. security.core.userdetails. User(
                user.getUsername(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeleteFlagFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User is inactive: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user. getRole().name()))
        );
    }
}