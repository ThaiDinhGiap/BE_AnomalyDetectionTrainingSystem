package com.sep490.anomaly_training_backend.security;

import com.sep490.anomaly_training_backend.dto.response.AuthResponse;
import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.impl.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet. http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security. web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org. springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository, @Lazy AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String oauthProviderId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            email = oAuth2User.getAttribute("preferred_username");
        }

        log.info("OAuth2 authentication success for:  {}", email);

        // Find user in database
        String finalEmail = email;
        User user = userRepository
                .findByOauthProviderAndOauthProviderIdAndDeleteFlagFalse(OAuthProvider.MICROSOFT, oauthProviderId)
                .orElseGet(() -> userRepository. findByEmailAndDeleteFlagFalse(finalEmail).orElse(null));

        if (user == null) {
            log.error("User not found after OAuth2 authentication: {}", email);
            handleFailure(response, "User not found");
            return;
        }

        if (!user.getIsActive()) {
            log.error("Inactive user attempted OAuth2 login: {}", email);
            handleFailure(response, "User account is inactive");
            return;
        }

        // Generate tokens
        AuthResponse authResponse = authService.generateAuthResponseForOAuth2User(user);

        // Redirect to frontend with tokens
        String frontendUrl = allowedOrigins. split(",")[0]; // Get first allowed origin
        String redirectUrl = UriComponentsBuilder
                . fromUriString(frontendUrl + "/oauth2/callback")
                .queryParam("accessToken", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .queryParam("expiresIn", authResponse.getExpiresIn())
                .build()
                .toUriString();

        log.info("Redirecting to frontend:  {}", frontendUrl + "/oauth2/callback");

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void handleFailure(HttpServletResponse response, String message) throws IOException {
        String frontendUrl = allowedOrigins. split(",")[0];
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/oauth2/callback")
                .queryParam("error", message)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}