package com.sep490.anomaly_training_backend.exception;

import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<String> VALIDATORS_ATTRIBUTES = Set.of(
            "fieldName", "max", "min", "maxFileSizeMb", "allowedTypesMessage"
    );

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        log.warn("App Exception: {} - Code: {}", e.getMessage(), e.getErrorCode().getCode());
        return buildErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.USER_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String enumKey = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        Map<String, Object> validationAttributes = null;
        try {
            validationAttributes = Objects.requireNonNull(e.getBindingResult().getFieldError())
                    .unwrap(ConstraintViolation.class)
                    .getConstraintDescriptor()
                    .getAttributes();
        } catch (Exception ex) {
            log.error("Cannot get validation attributes: {}", ex.getMessage());
        }
        return buildDynamicValidationResponse(enumKey, validationAttributes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        ConstraintViolation<?> constrainViolation = e.getConstraintViolations().iterator().next();
        String enumKey = constrainViolation.getMessage();
        Map<String, Object> validationAttributes = constrainViolation.getConstraintDescriptor().getAttributes();
        return buildDynamicValidationResponse(enumKey, validationAttributes);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = ErrorCode.INVALID_DATA_TYPE.getMessage().replace("{name}", e.getName());
        return buildErrorResponse(ErrorCode.INVALID_DATA_TYPE, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return buildErrorResponse(ErrorCode.INVALID_REQUEST_FORMAT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return buildErrorResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        log.error("Critical System Error: ", e);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode) {
        return buildErrorResponse(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .errorCode(String.valueOf(errorCode.getCode()))
                        .message(message)
                        .build());
    }

    private ResponseEntity<ApiResponse<Void>> buildDynamicValidationResponse(String enumKey, Map<String, Object> attributes) {
        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid ErrorCode key: {}", enumKey);
            return buildErrorResponse(ErrorCode.INVALID_ERROR_KEY);
        }

        String finalMessage = mapAttributeMessage(errorCode.getMessage(), attributes);
        return buildErrorResponse(errorCode, finalMessage);
    }

    private String mapAttributeMessage(String message, Map<String, Object> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                if (VALIDATORS_ATTRIBUTES.contains(entry.getKey())) {
                    message = message.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }
            }
        }
        return message;
    }
}