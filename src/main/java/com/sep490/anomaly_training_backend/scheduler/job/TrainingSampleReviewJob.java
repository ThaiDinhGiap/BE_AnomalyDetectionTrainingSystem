package com.sep490.anomaly_training_backend.scheduler.job;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class TrainingSampleReviewJob implements Job {
    private final TrainingSampleReviewConfigRepository  trainingSampleReviewConfigRepository;
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // Lấy configId từ JobDataMap
            Long configId = Long.parseLong(
                    context.getJobDetail().getJobDataMap().getString("configId")
            );

            log.info("=== Starting TrainingSampleReviewJob for configId: {} ===", configId);

            // 1. Lấy config từ DB
            TrainingSampleReviewConfig config = trainingSampleReviewConfigRepository.findById(configId)
                    .orElseThrow(() -> new JobExecutionException(
                            "Config not found: " + configId
                    ));

            // 2. Lấy ProductLine
            ProductLine productLine = config.getReviewPolicy().getProductLine();

            // 3. Tạo TrainingSampleReview mới
            TrainingSampleReview review = TrainingSampleReview.builder()
                    .config(config)
                    .productLine(productLine)
                    .startDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(config.getDueDays()))
                    .status(ReportStatus.NEED_ASSIGNED)
                    .build();

            TrainingSampleReview savedReview = trainingSampleReviewRepository.save(review);

            log.info("✓ Successfully created TrainingSampleReview id={} for configId={}",
                    savedReview.getId(), configId);

            log.info("=== Completed TrainingSampleReviewJob ===");

        } catch (Exception e) {
            log.error("❌ Error in TrainingSampleReviewJob", e);
            throw new JobExecutionException("Failed to create TrainingSampleReview", e);
        }
    }
}
