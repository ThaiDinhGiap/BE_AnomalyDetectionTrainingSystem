package com.sep490.anomaly_training_backend.aspect;

import com.sep490.anomaly_training_backend.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // ==================== SERVICE LOGGING ====================

    @Pointcut("execution(* com.sep490.anomaly_training_backend.service..*.*(..))")
    public void serviceLayer() {}

    @Before("serviceLayer()")
    public void logServiceMethodEntry(JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            log.debug(">>> Entering: {}.{}() with args={}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logServiceMethodExit(JoinPoint joinPoint, Object result) {
        if (log.isDebugEnabled()) {
            log.debug("<<< Exiting: {}.{}() with result={}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    result);
        }
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    public void logServiceMethodException(JoinPoint joinPoint, Throwable exception) {
        log.error("!!! Exception in {}.{}(): {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                exception.getMessage());
    }

    // ==================== APPROVAL AUDIT LOGGING ====================

    @Pointcut("execution(* com.sep490.anomaly_training_backend.service.approval.ApprovalService.*(..))")
    public void approvalService() {}

    @AfterReturning("approvalService()")
    public void logApprovalAction(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Log to AUDIT logger
        switch (methodName) {
            case "submit" -> LogUtils.audit("SUBMIT", extractEntityInfo(args));
            case "approve" -> LogUtils.audit("APPROVE", extractEntityInfo(args));
            case "reject" -> LogUtils.audit("REJECT", extractEntityInfo(args));
            case "revise" -> LogUtils.audit("REVISE", extractEntityInfo(args));
        }
    }

    // ==================== REPOSITORY LOGGING (DEBUG) ====================

    @Pointcut("execution(* com.sep490.anomaly_training_backend.repository..*.*(..))")
    public void repositoryLayer() {}

    @Around("repositoryLayer()")
    public Object logRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Log slow queries (> 500ms)
            if (duration > 500) {
                LogUtils.logSlowOperation(
                        joinPoint.getSignature().toShortString(),
                        duration,
                        500
                );
            }

            return result;
        } catch (Throwable e) {
            log.error("Repository error: {}.{}() - {}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage());
            throw e;
        }
    }

    // ==================== HELPER ====================

    private String extractEntityInfo(Object[] args) {
        if (args.length > 0) {
            Object firstArg = args[0];
            // Try to extract entity info
            return firstArg.toString();
        }
        return "N/A";
    }
}