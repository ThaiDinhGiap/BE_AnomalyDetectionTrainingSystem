package com.sep490.anomaly_training_backend.scheduler.job;

import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.TrainingSampleReviewConfig;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class TrainingSampleReviewJob implements Job {
    private final TrainingSampleReviewConfigRepository trainingSampleReviewConfigRepository;
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        try {
            // Lấy configId từ JobDataMap
            Long configId = Long.parseLong(
                    context.getJobDetail().getJobDataMap().getString("configId")
            );

            log.info("=== Starting TrainingSampleReviewJob for configId: {} ===", configId);

            // 1. Lấy config từ DB
            TrainingSampleReviewConfig config = trainingSampleReviewConfigRepository.findById(configId)
                    .orElseThrow(() -> new AppException(ErrorCode.CONFIG_NOT_FOUND));

            // 2. Lấy ProductLine
            ProductLine productLine = config.getReviewPolicy().getProductLine();

            // 3. Tạo TrainingSampleReview mới
            TrainingSampleReview review = TrainingSampleReview.builder()
                    .config(config)
                    .productLine(productLine)
                    .startDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(config.getDueDays()))
                    .status(ReportStatus.UNASSIGNED)
                    .build();

            TrainingSampleReview savedReview = trainingSampleReviewRepository.save(review);

            log.info("✓ Successfully created TrainingSampleReview id={} for configId={}",
                    savedReview.getId(), configId);

        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to execute TrainingSampleReviewJob");
        }
    }
}
