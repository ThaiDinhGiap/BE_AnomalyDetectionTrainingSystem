package com.sep490.anomaly_training_backend.scheduler;

import com.sep490.anomaly_training_backend.dto.request.SendInAppNotificationRequest;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.TrainingPlanDetailRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingReminderScheduler {

    private final TrainingPlanDetailRepository planDetailRepository;
    private final UserRepository userRepository;
    private final InAppNotificationService inAppService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Nhắc nhở lịch kiểm tra hôm nay (7:00) ───────────────────

    @Scheduled(cron = "${app.scheduler.training-reminder-today:0 0 7 * * ?}")
    @Transactional(readOnly = true)
    public void sendTodayTrainingReminders() {
        log.info("=== Starting Today Training Reminder Job ===");
        LocalDate today = LocalDate.now();

        try {
            List<TrainingPlanDetail> details = planDetailRepository
                    .findByPlannedDateAndResultStatusPending(today);

            if (details.isEmpty()) {
                log.info("No training scheduled for today: {}", today);
                return;
            }

            groupByTeamLeader(details).forEach((username, items) ->
                    sendTodayReminder(username, items, today));

            log.info("=== Completed Today Training Reminder Job: {} TLs notified ===",
                    groupByTeamLeader(details).size());

        } catch (Exception e) {
            log.error("Error in Today Training Reminder Job", e);
        }
    }

    // ── Nhắc nhở lịch kiểm tra ngày mai (17:00) ────────────────

    @Scheduled(cron = "${app.scheduler.training-reminder-upcoming:0 0 17 * * ?}")
    @Transactional(readOnly = true)
    public void sendUpcomingTrainingReminders() {
        log.info("=== Starting Upcoming Training Reminder Job ===");
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        try {
            List<TrainingPlanDetail> details = planDetailRepository
                    .findByPlannedDateAndResultStatusPending(tomorrow);

            if (details.isEmpty()) {
                log.info("No training scheduled for tomorrow: {}", tomorrow);
                return;
            }

            groupByTeamLeader(details).forEach((username, items) ->
                    sendUpcomingReminder(username, items, tomorrow));

            log.info("=== Completed Upcoming Training Reminder Job ===");

        } catch (Exception e) {
            log.error("Error in Upcoming Training Reminder Job", e);
        }
    }

    // ── Cảnh báo lịch kiểm tra quá hạn (8:00) ─────────────────

    @Scheduled(cron = "${app.scheduler.training-overdue-warning:0 0 8 * * ?}")
    @Transactional(readOnly = true)
    public void sendOverdueTrainingWarnings() {
        log.info("=== Starting Overdue Training Warning Job ===");
        LocalDate today = LocalDate.now();

        try {
            List<TrainingPlanDetail> overdueDetails = planDetailRepository
                    .findOverdueTrainings(today);

            if (overdueDetails.isEmpty()) {
                log.info("No overdue trainings found");
                return;
            }

            groupByTeamLeader(overdueDetails).forEach(this::sendOverdueWarning);

            log.info("=== Completed Overdue Training Warning Job: {} overdue items ===",
                    overdueDetails.size());

        } catch (Exception e) {
            log.error("Error in Overdue Training Warning Job", e);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Map<String, List<TrainingPlanDetail>> groupByTeamLeader(List<TrainingPlanDetail> details) {
        return details.stream()
                .collect(Collectors.groupingBy(d -> d.getTrainingPlan().getCreatedBy()));
    }

    private void sendTodayReminder(String username, List<TrainingPlanDetail> details, LocalDate date) {
        User tl = resolveTeamLeader(username);
        if (tl == null) return;

        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(tl.getId())
                        .title("Lịch kiểm tra hôm nay")
                        .message(String.format(
                                "Bạn có %d nhân viên cần kiểm tra hôm nay (%s). Vui lòng hoàn thành đúng tiến độ.",
                                details.size(), date.format(DATE_FMT)))
                        .type(InAppNotificationType.ACTION_REQUIRED)
                        .relatedEntityType("TRAINING_PLAN_DETAIL")
                        .actionUrl("/training-plan?date=" + date)
                        .build()
        );

        log.info("[Reminder] Today → TL {} | {} trainings", tl.getUsername(), details.size());
    }

    private void sendUpcomingReminder(String username, List<TrainingPlanDetail> details, LocalDate date) {
        User tl = resolveTeamLeader(username);
        if (tl == null) return;

        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(tl.getId())
                        .title("Lịch kiểm tra ngày mai")
                        .message(String.format(
                                "Ngày mai (%s) bạn có %d nhân viên trong kế hoạch kiểm tra.",
                                date.format(DATE_FMT), details.size()))
                        .type(InAppNotificationType.INFO)
                        .relatedEntityType("TRAINING_PLAN_DETAIL")
                        .actionUrl("/training-plan?date=" + date)
                        .build()
        );

        log.info("[Reminder] Upcoming → TL {} | {} trainings on {}", tl.getUsername(), details.size(), date);
    }

    private void sendOverdueWarning(String username, List<TrainingPlanDetail> details) {
        User tl = resolveTeamLeader(username);
        if (tl == null) return;

        inAppService.send(
                SendInAppNotificationRequest.builder()
                        .recipientId(tl.getId())
                        .title("Lịch kiểm tra quá hạn")
                        .message(String.format(
                                "Bạn có %d lịch kiểm tra đã quá hạn và chưa ghi nhận kết quả. Vui lòng xử lý ngay.",
                                details.size()))
                        .type(InAppNotificationType.WARNING)
                        .relatedEntityType("TRAINING_PLAN_DETAIL")
                        .actionUrl("/training-plan?filter=overdue")
                        .build()
        );

        log.info("[Reminder] Overdue warning → TL {} | {} items", tl.getUsername(), details.size());
    }

    private User resolveTeamLeader(String username) {
        User tl = userRepository.findByUsername(username).orElse(null);
        if (tl == null || !tl.getIsActive()) {
            log.warn("[Reminder] TL not found or inactive: {}", username);
            return null;
        }
        return tl;
    }
}