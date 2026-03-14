package com.sep490.anomaly_training_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableScheduling
public class AnomalyTrainingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnomalyTrainingBackendApplication.class, args);
    }

}
