package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.enums.TrainingSampleReviewResult;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
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

    /**
     * Check và mark training sample reviews quá hạn
     * Chạy lúc 7:00 hàng ngày
     *
     * Rule:
     * - Nếu dueDate < ngày hôm nay
     * - Và result != APPROVED
     * - Thì mark result = OVERDUE
     * - Gửi notification cho reviewer
     */
    @Scheduled(cron = "${app.scheduler.review-overdue: 0 0 7 * * ?}")
    @Transactional
    public void checkAndMarkOverdueReviews() {
        List<TrainingSampleReview> overdueReviews = trainingReviewRepository.findOverdueReviews();
        if (overdueReviews.isEmpty()) {
            log.info("✓ No overdue reviews found");
            return;
        }
        Map<Long, OverdueReviewSummary> reviewerPendings = new HashMap<>();
        for (TrainingSampleReview review : overdueReviews) {
            review.setResult(TrainingSampleReviewResult.OVERDUE);
            trainingReviewRepository.save(review);
            Long reviewerId = review.getReviewedBy().getId();
            reviewerPendings.computeIfAbsent(reviewerId, k -> new OverdueReviewSummary())
                            .addReview(review);
        }
        sendOverdueNotifications(reviewerPendings);
    }


    /**
     * Gửi notification cho các reviewer về các review đã overdue
     *
     * @param reviewerPendings Map reviewer -> list reviews
     */
    private void sendOverdueNotifications(Map<Long, OverdueReviewSummary> reviewerPendings) {
        log.info("Sending overdue notifications to {} reviewers", reviewerPendings.size());

        for (Map.Entry<Long, OverdueReviewSummary> entry : reviewerPendings.entrySet()) {
            Long reviewerId = entry.getKey();
            OverdueReviewSummary summary = entry.getValue();

            try {
                sendNotificationToReviewer(reviewerId, summary);
            } catch (Exception e) {
                log.error("Failed to send notification to reviewer {}: {}",
                        reviewerId, e.getMessage());
            }
        }
    }

    /**
     * Gửi notification cho reviewer
     *
     * @param reviewerId reviewer ID
     * @param summary summary of overdue reviews
     */
    private void sendNotificationToReviewer(Long reviewerId, OverdueReviewSummary summary) {
        User reviewer = userRepository.findById(reviewerId).orElse(null);

        if (reviewer == null || reviewer.getEmail() == null) {
            log.warn("Cannot send notification to reviewer {}: user not found or no email", reviewerId);
            return;
        }

        String body = buildOverdueNotificationBody(reviewer, summary);

        NotificationRequest request = NotificationRequest.builder()
                .recipientUserId(reviewerId)
                .recipientEmail(reviewer.getEmail())
                .recipientName(reviewer.getFullName())
                .type(NotificationType.SAMPLE_REVIEW_OVERDUE)
                .channel(NotificationChannel.EMAIL)
                .build();

        notificationService.sendNotification(request);

        log.info("✓ Sent overdue review notification to {} ({}) for {} reviews",
                reviewer.getFullName(), reviewer.getEmail(), summary.getTotalCount());
    }

    /**
     * Build nội dung email notification
     *
     * @param reviewer reviewer
     * @param summary summary of overdue reviews
     * @return email body
     */
    private String buildOverdueNotificationBody(User reviewer, OverdueReviewSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kính gửi ").append(reviewer.getFullName()).append(",\n\n");
        sb.append("Bạn có ").append(summary.getTotalCount()).append(" đánh giá mẫu huấn luyện đã quá hạn:\n\n");

        // Liệt kê các production line
        summary.getReviews().stream()
                .map(r -> r.getProductLine().getName())
                .distinct()
                .forEach(line -> sb.append("- Dây chuyền: ").append(line).append("\n"));

        // Thêm thông tin về earliest overdue date
        if (!summary.getReviews().isEmpty()) {
            LocalDate earliestDueDate = summary.getReviews().stream()
                    .map(TrainingSampleReview::getDueDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            sb.append("\nNgày quá hạn sớm nhất: ").append(earliestDueDate).append("\n");
        }

        sb.append("\nVui lòng đăng nhập hệ thống để hoàn thành đánh giá.\n");
        sb.append("\nTrân trọng,\nHệ thống Anomaly Training");

        return sb.toString();
    }

    /**
     * Inner class để hold summary of overdue reviews per reviewer
     */
    private static class OverdueReviewSummary {
        private final List<TrainingSampleReview> reviews = new ArrayList<>();

        public void addReview(TrainingSampleReview review) {
            reviews.add(review);
        }

        public List<TrainingSampleReview> getReviews() {
            return reviews;
        }

        public int getTotalCount() {
            return reviews.size();
        }
    }


}
