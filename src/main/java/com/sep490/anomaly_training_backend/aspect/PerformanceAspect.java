package com.sep490.anomaly_training_backend.aspect;

import com.sep490.anomaly_training_backend.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Value("${app.performance.slow-threshold-ms:1000}")
    private long slowThresholdMs;

    @Pointcut("@annotation(com.sep490.anomaly_training_backend.annotation.LogPerformance)")
    public void performanceAnnotated() {}

    @Pointcut("execution(* com.sep490.anomaly_training_backend.controller..*.*(..))")
    public void controllerLayer() {}

    @Around("performanceAnnotated() || controllerLayer()")
    public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            if (duration > slowThresholdMs) {
                LogUtils.logSlowOperation(
                        joinPoint.getSignature().toShortString(),
                        duration,
                        slowThresholdMs
                );
            }
        }
    }
}