package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import com.sep490.anomaly_training_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class TrainingSampleReviewOverdueScheduler {

    private final TrainingSampleReviewRepository trainingReviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final InAppNotificationService inAppService;

    @Scheduled(cron = "${app.scheduler.review-overdue: 0 0 7 * * ?}")
    @Transactional
    public void checkAndMarkOverdueReviews() {
        log.info("=== Starting TrainingSampleReview Overdue Job ===");

        List<TrainingSampleReview> overdueReviews = trainingReviewRepository.findOverdueReviews();

        if (overdueReviews.isEmpty()) {
            log.info("✓ No overdue reviews found");
            return;
        }

        Map<Long, List<TrainingSampleReview>> byReviewer = new HashMap<>();

        for (TrainingSampleReview review : overdueReviews) {
            review.setStatus(ReportStatus.MISSED);
            trainingReviewRepository.save(review);

            byReviewer.computeIfAbsent(review.getReviewedBy().getId(), k -> new ArrayList<>())
                    .add(review);
        }

        log.info("Marked {} reviews as MISS, notifying {} reviewers",
                overdueReviews.size(), byReviewer.size());

        byReviewer.forEach(this::notifyReviewer);

        log.info("=== Completed TrainingSampleReview Overdue Job ===");
    }

    private void notifyReviewer(Long reviewerId, List<TrainingSampleReview> reviews) {
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        if (reviewer == null) {
            log.warn("[ReviewOverdue] Reviewer {} not found", reviewerId);
            return;
        }

        if (reviewer.getEmail() != null) {
            notificationService.sendNotification(
                    NotificationRequest.builder()
                            .recipientUserId(reviewerId)
                            .recipientEmail(reviewer.getEmail())
                            .recipientName(reviewer.getFullName())
                            .type(NotificationType.SAMPLE_REVIEW_OVERDUE)
                            .channel(NotificationChannel.EMAIL)
                            .build()
            );
        }

        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(reviewerId)
                        .title("Đánh giá mẫu huấn luyện quá hạn")
                        .message(buildInAppMessage(reviews))
                        .type(InAppNotificationType.WARNING)
                        .relatedEntityType("TRAINING_SAMPLE_REVIEW")
                        .actionUrl("/sample-reviews?filter=overdue")
                        .build()
        );

        log.info("[ReviewOverdue] Notified reviewer {} — {} reviews", reviewer.getFullName(), reviews.size());
    }

    private String buildInAppMessage(List<TrainingSampleReview> reviews) {
        List<String> productLines = reviews.stream()
                .map(r -> r.getProductLine().getName())
                .distinct()
                .toList();

        LocalDate earliest = reviews.stream()
                .map(TrainingSampleReview::getDueDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        if (productLines.size() == 1) {
            return String.format(
                    "Bạn có %d đánh giá mẫu quá hạn tại dây chuyền %s (hạn sớm nhất: %s).",
                    reviews.size(), productLines.get(0), earliest);
        }

        return String.format(
                "Bạn có %d đánh giá mẫu quá hạn tại %d dây chuyền (hạn sớm nhất: %s).",
                reviews.size(), productLines.size(), earliest);
    }
}