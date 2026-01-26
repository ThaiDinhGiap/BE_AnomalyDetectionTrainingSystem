package com.sep490.anomaly_training_backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Spring Scheduler được enable
    // Các scheduled tasks sẽ chạy theo cron expression
}
