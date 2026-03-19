package com.sep490.anomaly_training_backend.scheduler;


import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewPolicy;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewPolicyRepository;
import com.sep490.anomaly_training_backend.scheduler.job.TrainingSampleReviewJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleReviewScheduler {

    private final Scheduler scheduler;
    private final TrainingSampleReviewConfigRepository configRepository;
    private final TrainingSampleReviewPolicyRepository policyRepository;

    public void registerJob(TrainingSampleReviewConfig config) {
        try {
            Long configId = config.getId();
            String cron = CronExpressionBuilder.buildCronExpression(
                    config.getTriggerDay(),
                    config.getTriggerMonth()
            );

            log.info("Registering job for configId={}, cron={}", configId, cron);

            // 1. Tạo JobDetail
            JobDetail jobDetail = JobBuilder.newJob(TrainingSampleReviewJob.class)
                    .withIdentity(buildJobKey(configId))
                    .usingJobData("configId", configId.toString())
                    .withDescription("Auto-create TrainingSampleReview for config " + configId)
                    .build();

            // 2. Tạo Trigger với cron expression
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(buildTriggerKey(configId))
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .withDescription("Trigger for config " + configId)
                    .build();

            // 3. Đăng ký với scheduler
            scheduler.scheduleJob(jobDetail, trigger);

            log.info("✓ Successfully registered job for configId={}", configId);

        } catch (SchedulerException e) {
            log.error("❌ Failed to register job for config", e);
            throw new RuntimeException("Failed to register scheduled job", e);
        }
    }

    /**
     * Cập nhật job khi config thay đổi
     *
     * @param config config đã update
     */
    public void updateJob(TrainingSampleReviewConfig config) {
        try {
            Long configId = config.getId();

            // 1. Xóa job cũ
            removeJob(configId);

            // 2. Tạo job mới
            registerJob(config);

            log.info("✓ Successfully updated job for configId={}", configId);

        } catch (Exception e) {
            log.error("❌ Failed to update job for config", e);
            throw new RuntimeException("Failed to update scheduled job", e);
        }
    }

    /**
     * Xóa job theo configId
     *
     * @param configId ID của config
     */
    public void removeJob(Long configId) {
        try {
            JobKey jobKey = buildJobKey(configId);
            boolean deleted = scheduler.deleteJob(jobKey);

            if (deleted) {
                log.info("✓ Successfully removed job for configId={}", configId);
            } else {
                log.warn("Job not found for configId={}", configId);
            }

        } catch (SchedulerException e) {
            log.error("❌ Failed to remove job for configId={}", configId, e);
            throw new RuntimeException("Failed to remove scheduled job", e);
        }
    }

    /**
     * Xóa tất cả job liên quan đến 1 policy
     *
     * @param policyId ID của policy
     */
//    public void removeAllJobsByPolicy(Long policyId) {
//        try {
//            // Lấy tất cả config của policy này
//            List<TrainingSampleReviewConfig> configs = configRepository
//                    .findByReviewPolicyId(policyId);
//
//            log.info("Removing {} jobs for policyId={}", configs.size(), policyId);
//
//            for (TrainingSampleReviewConfig config : configs) {
//                removeJob(config.getId());
//            }
//
//            log.info("✓ Successfully removed all jobs for policyId={}", policyId);
//
//        } catch (Exception e) {
//            log.error("❌ Failed to remove jobs for policyId={}", policyId, e);
//            throw new RuntimeException("Failed to remove scheduled jobs", e);
//        }
//    }

    /**
     * Khởi tạo lại tất cả job khi app start
     *
     * Quét DB để tìm tất cả active policy/config, sau đó đăng ký lại job
     */
//    @PostConstruct
//    public void initAllJobsOnStartup() {
//        try {
//            log.info("=== Initializing all TrainingSampleReview jobs on startup ===");
//
//            // Lấy tất cả active policy (deleteFlag = false, status = ACTIVE)
//            List<TrainingSampleReviewPolicy> activePolicies = policyRepository
//                    .findByDeleteFlagFalseAndStatusActive();
//
//            int jobCount = 0;
//
//            for (TrainingSampleReviewPolicy policy : activePolicies) {
//                for (TrainingSampleReviewConfig config : policy.getReviewConfigs()) {
//                    try {
//                        registerJob(config);
//                        jobCount++;
//                    } catch (Exception e) {
//                        log.error("Failed to register job for config {}", config.getId(), e);
//                    }
//                }
//            }
//
//            log.info("✓ Successfully initialized {} TrainingSampleReview jobs", jobCount);
//            log.info("=== Completed job initialization ===");
//
//        } catch (Exception e) {
//            log.error("❌ Error during job initialization", e);
//        }
//    }

    // ============ Private Helper Methods ============

    private JobKey buildJobKey(Long configId) {
        return new JobKey("trainingReviewJob_" + configId, "TRAINING_SAMPLE_REVIEW");
    }

    private TriggerKey buildTriggerKey(Long configId) {
        return new TriggerKey("trainingReviewTrigger_" + configId, "TRAINING_SAMPLE_REVIEW");
    }
}
