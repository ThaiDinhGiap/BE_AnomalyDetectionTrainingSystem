package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewPolicyRepository;
import com.sep490.anomaly_training_backend.scheduler.job.TrainingSampleReviewJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

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
            log.error("Failed to register job for config", e);
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
            log.error("Failed to update job for config", e);
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
            log.error("Failed to remove job for configId={}", configId, e);
            throw new RuntimeException("Failed to remove scheduled job", e);
        }
    }


    // ============ Private Helper Methods ============

    private JobKey buildJobKey(Long configId) {
        return new JobKey("trainingReviewJob_" + configId, "TRAINING_SAMPLE_REVIEW");
    }

    private TriggerKey buildTriggerKey(Long configId) {
        return new TriggerKey("trainingReviewTrigger_" + configId, "TRAINING_SAMPLE_REVIEW");
    }
}
