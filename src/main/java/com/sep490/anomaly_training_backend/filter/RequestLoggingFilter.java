package com.sep490.anomaly_training_backend.filter;

import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.util.LogUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    // Paths to skip logging
    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/actuator",
            "/swagger",
            "/v3/api-docs",
            "/favicon.ico"
    );

    // Sensitive headers to mask
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "Authorization",
            "Cookie",
            "X-Auth-Token"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip certain paths
        if (shouldSkip(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate trace ID
        String traceId = LogUtils.generateTraceId();
        
        // Set request context
        String clientIp = getClientIp(request);
        LogUtils.setRequestContext(request.getRequestURI(), clientIp);

        // Wrap request/response for body logging
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            logRequest(wrappedRequest, traceId, clientIp);

            // Set user context if authenticated
            setUserContextFromAuth();

            // Process request
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            logResponse(wrappedRequest, wrappedResponse, duration, traceId);

            // Log performance
            LogUtils.logApiPerformance(
                    request.getMethod(),
                    request.getRequestURI(),
                    wrappedResponse.getStatus(),
                    duration
            );

            // Copy response body back
            wrappedResponse.copyBodyToResponse();

            // Clear MDC
            LogUtils.clearContext();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String traceId, String clientIp) {
        String queryString = request.getQueryString();
        String uri = queryString != null 
                ? request.getRequestURI() + "?" + queryString 
                : request.getRequestURI();

        log.info(">>> REQUEST | traceId={} | method={} | uri={} | ip={} | userAgent={}",
                traceId,
                request.getMethod(),
                uri,
                clientIp,
                request.getHeader("User-Agent")
        );

        // Log request body for POST/PUT (exclude sensitive endpoints)
        if (shouldLogBody(request)) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                // Mask sensitive fields
                body = maskSensitiveData(body);
                log.debug(">>> REQUEST BODY | traceId={} | body={}", traceId, body);
            }
        }
    }

    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             long duration,
                             String traceId) {

        int status = response.getStatus();
        String level = status >= 500 ? "ERROR" : (status >= 400 ? "WARN" : "INFO");

        if (status >= 400) {
            log.warn("<<< RESPONSE | traceId={} | status={} | duration={}ms | uri={}",
                    traceId, status, duration, request.getRequestURI());
        } else {
            log.info("<<< RESPONSE | traceId={} | status={} | duration={}ms",
                    traceId, status, duration);
        }
    }

    private void setUserContextFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            LogUtils.setUserContext(user.getId(), user.getUsername());
        }
    }

    private boolean shouldSkip(String uri) {
        return SKIP_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean shouldLogBody(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Only log body for POST/PUT/PATCH
        if (!Arrays.asList("POST", "PUT", "PATCH").contains(method)) {
            return false;
        }

        // Skip sensitive endpoints
        if (uri.contains("/login") || uri.contains("/pin") || uri.contains("/password")) {
            return false;
        }

        return true;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content);
        }
        return null;
    }

    private String maskSensitiveData(String body) {
        // Mask password, pin, token fields
        return body
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("\"pin\"\\s*:\\s*\"[^\"]*\"", "\"pin\":\"***\"")
                .replaceAll("\"oldPin\"\\s*:\\s*\"[^\"]*\"", "\"oldPin\":\"***\"")
                .replaceAll("\"newPin\"\\s*:\\s*\"[^\"]*\"", "\"newPin\":\"***\"")
                .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Get first IP if multiple
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}