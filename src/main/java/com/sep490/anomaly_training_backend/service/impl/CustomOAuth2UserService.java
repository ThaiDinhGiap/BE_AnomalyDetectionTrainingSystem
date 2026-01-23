package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core. OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("microsoft".equals(registrationId)) {
            return processMicrosoftUser(oAuth2User);
        }

        return oAuth2User;
    }

    private OAuth2User processMicrosoftUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String oauthProviderId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) {
            // Fallback to preferred_username if email not available
            email = (String) attributes.get("preferred_username");
        }

        if (oauthProviderId == null || email == null) {
            log.error("Missing required attributes from Microsoft:  sub={}, email={}", oauthProviderId, email);
            throw new OAuth2AuthenticationException("Missing required user information from Microsoft");
        }

        log.info("Processing Microsoft OAuth2 user: email={}, name={}", email, name);

        // Find or create user
        Optional<User> existingUser = userRepository
                .findByOauthProviderAndOauthProviderIdAndDeleteFlagFalse(OAuthProvider.MICROSOFT, oauthProviderId);

        if (existingUser.isPresent()) {
            log.info("Existing Microsoft user found: {}", email);
            return oAuth2User;
        }

        // Check if user exists with same email (link accounts)
        Optional<User> userByEmail = userRepository. findByEmailAndDeleteFlagFalse(email);

        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            // Link Microsoft account to existing user
            user. setOauthProvider(OAuthProvider.MICROSOFT);
            user.setOauthProviderId(oauthProviderId);
            userRepository.save(user);
            log.info("Linked Microsoft account to existing user: {}", email);
            return oAuth2User;
        }

        // Create new user
        String username = generateUsername(email);

        User newUser = User.builder()
                .username(username)
                .email(email)
                .fullName(name != null ? name : email)
                .role(UserRole.TEAM_LEADER) // Default role - có thể điều chỉnh
                . oauthProvider(OAuthProvider.MICROSOFT)
                .oauthProviderId(oauthProviderId)
                .isActive(true)
                .build();

        userRepository.save(newUser);
        log.info("Created new Microsoft user: {}", email);

        return oAuth2User;
    }

    private String generateUsername(String email) {
        // Extract username from email (before @)
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        // Ensure unique username
        while (userRepository. existsByUsernameAndDeleteFlagFalse(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}