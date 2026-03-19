package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.notification.NotificationRequest;
import com.sep490.anomaly_training_backend.enums.NotificationChannel;
import com.sep490.anomaly_training_backend.enums.NotificationType;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.TrainingPlanDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewConfigRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class TrainingReminderScheduler {

    private final TrainingPlanDetailRepository planDetailRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.scheduler.training-reminder-today:0 0 7 * * ?}")
    @Transactional(readOnly = true)
    public void sendTodayTrainingReminders() {
        log.info("=== Starting Today Training Reminder Job ===");

        LocalDate today = LocalDate.now();

        try {
            List<TrainingPlanDetail> todayTrainings = planDetailRepository
                    .findByPlannedDateAndResultStatusPending(today);

            if (todayTrainings.isEmpty()) {
                log.info("No training scheduled for today:  {}", today);
                return;
            }

            // Group theo Team Leader (người tạo plan)
            Map<String, List<TrainingPlanDetail>> groupedByTeamLeader = todayTrainings.stream()
                    .collect(Collectors.groupingBy(detail ->
                            detail.getTrainingPlan().getCreatedBy()));

            log.info("Found {} trainings for {} team leaders today",
                    todayTrainings.size(), groupedByTeamLeader.size());

            // Gửi notification cho từng Team Leader
            for (Map.Entry<String, List<TrainingPlanDetail>> entry : groupedByTeamLeader.entrySet()) {
                String teamLeaderUsername = entry.getKey();
                List<TrainingPlanDetail> details = entry.getValue();

                sendTodayReminderToTeamLeader(teamLeaderUsername, details, today);
            }

            log.info("=== Completed Today Training Reminder Job ===");

        } catch (Exception e) {
            log.error("Error in Today Training Reminder Job", e);
        }
    }

    /**
     * Use Case 3a (variant): Nhắc nhở lịch kiểm tra ngày mai
     * Chạy lúc 17:00 chiều hàng ngày
     */
    @Scheduled(cron = "${app.scheduler.training-reminder-upcoming:0 0 17 * * ?}")
    @Transactional(readOnly = true)
    public void sendUpcomingTrainingReminders() {
        log.info("=== Starting Upcoming Training Reminder Job ===");

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        try {
            List<TrainingPlanDetail> tomorrowTrainings = planDetailRepository
                    .findByPlannedDateAndResultStatusPending(tomorrow);

            if (tomorrowTrainings.isEmpty()) {
                log.info("No training scheduled for tomorrow: {}", tomorrow);
                return;
            }

            Map<String, List<TrainingPlanDetail>> groupedByTeamLeader = tomorrowTrainings.stream()
                    .collect(Collectors.groupingBy(detail ->
                            detail.getTrainingPlan().getCreatedBy()));

            for (Map.Entry<String, List<TrainingPlanDetail>> entry : groupedByTeamLeader.entrySet()) {
                String teamLeaderUsername = entry.getKey();
                List<TrainingPlanDetail> details = entry.getValue();

                sendUpcomingReminderToTeamLeader(teamLeaderUsername, details, tomorrow);
            }

            log.info("=== Completed Upcoming Training Reminder Job ===");

        } catch (Exception e) {
            log.error("Error in Upcoming Training Reminder Job", e);
        }
    }

    /**
     * Use Case 3b: Cảnh báo lịch kiểm tra quá hạn
     * Chạy lúc 8:00 sáng hàng ngày
     */
    @Scheduled(cron = "${app.scheduler.training-overdue-warning:0 0 8 * * ?}")
    @Transactional(readOnly = true)
    public void sendOverdueTrainingWarnings() {
        log.info("=== Starting Overdue Training Warning Job ===");

        LocalDate today = LocalDate.now();

        try {
            // Lấy tất cả training đã quá hạn (planned_date < today) và chưa ghi nhận kết quả
            List<TrainingPlanDetail> overdueTrainings = planDetailRepository
                    .findOverdueTrainings(today);

            if (overdueTrainings.isEmpty()) {
                log.info("No overdue trainings found");
                return;
            }

            Map<String, List<TrainingPlanDetail>> groupedByTeamLeader = overdueTrainings.stream()
                    .collect(Collectors.groupingBy(detail ->
                            detail.getTrainingPlan().getCreatedBy()));

            log.info("Found {} overdue trainings for {} team leaders",
                    overdueTrainings.size(), groupedByTeamLeader.size());

            for (Map.Entry<String, List<TrainingPlanDetail>> entry : groupedByTeamLeader.entrySet()) {
                String teamLeaderUsername = entry.getKey();
                List<TrainingPlanDetail> details = entry.getValue();

                sendOverdueWarningToTeamLeader(teamLeaderUsername, details);
            }

            log.info("=== Completed Overdue Training Warning Job ===");

        } catch (Exception e) {
            log.error("Error in Overdue Training Warning Job", e);
        }
    }

    // ============================================
    // Private helper methods
    // ============================================

    private void sendTodayReminderToTeamLeader(String teamLeaderUsername, List<TrainingPlanDetail> details, LocalDate today) {
        User teamLeader = userRepository.findByUsername(teamLeaderUsername).orElse(null);
        if (teamLeader == null || !teamLeader.getIsActive()) {
            log.warn("Team leader not found or inactive: {}", teamLeaderUsername);
            return;
        }

        // Build training items list cho template
        List<Map<String, Object>> trainingItems = details.stream()
                .map(detail -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("employeeCode", detail.getEmployee().getEmployeeCode());
                    item.put("employeeName", detail.getEmployee().getFullName());
//                    item.put("processName", detail.getProcess().getName());
//                    item.put("processCode", detail.getProcess().getCode());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", teamLeader.getFullName());
        variables.put("today", today);
        variables.put("trainingCount", details.size());
        variables.put("trainingItems", trainingItems);
        variables.put("dashboardLink", buildDashboardLink(today));

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.TRAINING_REMINDER_TODAY)
                .recipientUserId(teamLeader.getId())
                .recipientEmail(teamLeader.getEmail())
                .recipientName(teamLeader.getFullName())
                .variables(variables)
                .channel(NotificationChannel.EMAIL)
                .priority(7) // High priority
                .build();

        notificationService.sendNotification(request);

        log.info("Sent today reminder to TL {} for {} trainings",
                teamLeader.getUsername(), details.size());
    }

    private void sendUpcomingReminderToTeamLeader(String teamLeaderUsername, List<TrainingPlanDetail> details, LocalDate date) {
        User teamLeader = userRepository.findByUsername(teamLeaderUsername).orElse(null);
        if (teamLeader == null || !teamLeader.getIsActive()) {
            return;
        }

        List<Map<String, Object>> trainingItems = details.stream()
                .map(detail -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("employeeCode", detail.getEmployee().getEmployeeCode());
                    item.put("employeeName", detail.getEmployee().getFullName());
//                    item.put("processName", detail.getProcess().getName());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", teamLeader.getFullName());
        variables.put("trainingDate", date);
        variables.put("trainingCount", details.size());
        variables.put("trainingItems", trainingItems);
        variables.put("dashboardLink", buildDashboardLink(date));

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.TRAINING_REMINDER_UPCOMING)
                .recipientUserId(teamLeader.getId())
                .recipientEmail(teamLeader.getEmail())
                .recipientName(teamLeader.getFullName())
                .variables(variables)
                .channel(NotificationChannel.EMAIL)
                .priority(5) // Normal priority
                .build();

        notificationService.sendNotification(request);

        log.info("Sent upcoming reminder to TL {} for {} trainings on {}",
                teamLeader.getUsername(), details.size(), date);
    }

    private void sendOverdueWarningToTeamLeader(String teamLeaderUsername, List<TrainingPlanDetail> details) {
        User teamLeader = userRepository.findByUsername(teamLeaderUsername).orElse(null);
        if (teamLeader == null || !teamLeader.getIsActive()) {
            return;
        }

        List<Map<String, Object>> overdueItems = details.stream()
                .map(detail -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("employeeCode", detail.getEmployee().getEmployeeCode());
                    item.put("employeeName", detail.getEmployee().getFullName());
//                    item.put("processName", detail.getProcess().getName());
                    item.put("plannedDate", detail.getPlannedDate());
                    item.put("daysOverdue", java.time.temporal.ChronoUnit.DAYS.between(
                            detail.getPlannedDate(), LocalDate.now()));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", teamLeader.getFullName());
        variables.put("overdueCount", details.size());
        variables.put("overdueItems", overdueItems);
        variables.put("dashboardLink", buildDashboardLink(null));

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.TRAINING_OVERDUE_WARNING)
                .recipientUserId(teamLeader.getId())
                .recipientEmail(teamLeader.getEmail())
                .recipientName(teamLeader.getFullName())
                .variables(variables)
                .channel(NotificationChannel.EMAIL)
                .priority(9) // Very high priority
                .build();

        notificationService.sendNotification(request);

        log.info("Sent overdue warning to TL {} for {} trainings",
                teamLeader.getUsername(), details.size());
    }

    private String buildDashboardLink(LocalDate date) {
        String baseUrl = "http://localhost:3000/training-plan";
        if (date != null) {
            return baseUrl + "?date=" + date;
        }
        return baseUrl + "?filter=overdue";
    }
}