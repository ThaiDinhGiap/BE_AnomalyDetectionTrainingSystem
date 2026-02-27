package com.sep490.anomaly_training_backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Centralized logging utilities
 */
public final class LogUtils {

    // Dedicated loggers cho từng mục đích
    public static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    public static final Logger SECURITY = LoggerFactory.getLogger("SECURITY");
    public static final Logger PERFORMANCE = LoggerFactory.getLogger("PERFORMANCE");

    // MDC keys
    public static final String TRACE_ID = "traceId";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String REQUEST_URI = "requestUri";
    public static final String CLIENT_IP = "clientIp";

    private LogUtils() {
        // Utility class
    }

    // ==================== MDC MANAGEMENT ====================

    /**
     * Tạo trace ID mới cho request
     */
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    /**
     * Set user context vào MDC
     */
    public static void setUserContext(Long userId, String username) {
        if (userId != null) {
            MDC.put(USER_ID, userId.toString());
        }
        if (username != null) {
            MDC.put(USERNAME, username);
        }
    }

    /**
     * Set request context vào MDC
     */
    public static void setRequestContext(String uri, String clientIp) {
        if (uri != null) {
            MDC.put(REQUEST_URI, uri);
        }
        if (clientIp != null) {
            MDC.put(CLIENT_IP, clientIp);
        }
    }

    /**
     * Clear MDC sau khi request xong
     */
    public static void clearContext() {
        MDC.clear();
    }

    // ==================== AUDIT LOGGING ====================

    /**
     * Log audit event
     */
    public static void audit(String action, String entityType, Long entityId, String detail) {
        AUDIT.info("ACTION={} | ENTITY={}:{} | DETAIL={}", 
                action, entityType, entityId, detail);
    }

    public static void audit(String action, String detail) {
        AUDIT.info("ACTION={} | DETAIL={}", action, detail);
    }

    // ==================== SECURITY LOGGING ====================

    /**
     * Log login success
     */
    public static void logLoginSuccess(String username, String ipAddress) {
        SECURITY.info("LOGIN_SUCCESS | user={} | ip={}", username, ipAddress);
    }

    /**
     * Log login failed
     */
    public static void logLoginFailed(String username, String ipAddress, String reason) {
        SECURITY.warn("LOGIN_FAILED | user={} | ip={} | reason={}", username, ipAddress, reason);
    }

    /**
     * Log logout
     */
    public static void logLogout(String username) {
        SECURITY.info("LOGOUT | user={}", username);
    }

    /**
     * Log access denied
     */
    public static void logAccessDenied(String username, String resource, String reason) {
        SECURITY.warn("ACCESS_DENIED | user={} | resource={} | reason={}", username, resource, reason);
    }

    /**
     * Log PIN events
     */
    public static void logPinSetup(String username) {
        SECURITY.info("PIN_SETUP | user={}", username);
    }

    public static void logPinVerifyFailed(String username, int failedAttempts) {
        SECURITY.warn("PIN_VERIFY_FAILED | user={} | failedAttempts={}", username, failedAttempts);
    }

    public static void logPinLocked(String username) {
        SECURITY.warn("PIN_LOCKED | user={}", username);
    }

    // ==================== PERFORMANCE LOGGING ====================

    /**
     * Log slow operation
     */
    public static void logSlowOperation(String operation, long durationMs, long thresholdMs) {
        PERFORMANCE.warn("SLOW_OPERATION | operation={} | duration={}ms | threshold={}ms",
                operation, durationMs, thresholdMs);
    }

    /**
     * Log API performance
     */
    public static void logApiPerformance(String method, String uri, int status, long durationMs) {
        if (durationMs > 1000) {
            PERFORMANCE.warn("SLOW_API | method={} | uri={} | status={} | duration={}ms",
                    method, uri, status, durationMs);
        } else {
            PERFORMANCE.info("API | method={} | uri={} | status={} | duration={}ms",
                    method, uri, status, durationMs);
        }
    }
}