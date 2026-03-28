package com.sep490.anomaly_training_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequirePasswordChangeFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // 1. Let public endpoints or change password / logout pass through
        if (isAllowedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Check current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            
            // 3. If requirePasswordChange is true, block the request
            if (Boolean.TRUE.equals(user.getRequirePasswordChange())) {
                log.warn("Blocked request to page {} because user {} must change password first", path, user.getUsername());
                handleException(response);
                return;
            }
        }

        // Allow passing through
        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        // Allow all public/auth endpoints so they can login, logout, change password, or refresh token
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/") ||
                path.equals("/error") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }

    private void handleException(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        ApiResponse<Void> apiResponse = ApiResponse.error("You must change your password before you can use the system.", String.valueOf(HttpStatus.FORBIDDEN.value()));

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
