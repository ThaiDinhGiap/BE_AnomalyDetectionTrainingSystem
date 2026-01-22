package com.denso.anomaly_training_backend.scheduler;

import com.denso.anomaly_training_backend.dto.notification.NotificationRequest;
import com.denso.anomaly_training_backend.enums.IssueReportStatus;
import com.denso.anomaly_training_backend.enums.NotificationChannel;
import com.denso.anomaly_training_backend.enums.NotificationType;
import com.denso.anomaly_training_backend.enums.TrainingPlanStatus;
import com.denso.anomaly_training_backend.enums.TrainingResultDetailStatus;
import com.denso.anomaly_training_backend.enums.TrainingResultStatus;
import com.denso.anomaly_training_backend.enums.TrainingTopicStatus;
import com.denso.anomaly_training_backend.model.IssueReport;
import com.denso.anomaly_training_backend.model.TrainingPlan;
import com.denso.anomaly_training_backend.model.TrainingResult;
import com.denso.anomaly_training_backend.model.TrainingResultDetail;
import com.denso.anomaly_training_backend.model.TrainingTopic;
import com.denso.anomaly_training_backend.model.User;
import com.denso.anomaly_training_backend.repository.IssueReportRepository;
import com.denso.anomaly_training_backend.repository.TrainingPlanRepository;
import com.denso.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.denso.anomaly_training_backend.repository.TrainingResultRepository;
import com.denso.anomaly_training_backend.repository.TrainingTopicRepository;
import com.denso.anomaly_training_backend.repository.UserRepository;
import com.denso.anomaly_training_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class ApprovalOverdueScheduler {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingResultRepository trainingResultRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final IssueReportRepository issueReportRepository;
    private final TrainingTopicRepository trainingTopicRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.scheduler.approval-overdue-hours:24}")
    private int overdueHours; // Sau bao nhiêu giờ thì coi là tồn đọng

    /**
     * Use Case 5: Nhắc nhở phê duyệt tồn đọng
     * Chạy lúc 8:00 và 14:00 hàng ngày
     */
    @Scheduled(cron = "${app.scheduler.approval-overdue: 0 0 8,14 * * ?}")
    @Transactional(readOnly = true)
    public void checkAndSendOverdueApprovalReminders() {
        log.info("=== Starting Approval Overdue Check Job ===");

        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(overdueHours);

        try {
            // 1. Check Supervisor overdue approvals
            checkSupervisorOverdueApprovals(overdueThreshold);

            // 2. Check Manager overdue approvals
            checkManagerOverdueApprovals(overdueThreshold);

            // 3. Check FI overdue confirmations
            checkFIOverdueConfirmations(overdueThreshold);

            log.info("=== Completed Approval Overdue Check Job ===");

        } catch (Exception e) {
            log.error("Error in Approval Overdue Check Job", e);
        }
    }

    // ============================================
    // Check Supervisor Overdue Approvals
    // ============================================

    private void checkSupervisorOverdueApprovals(LocalDateTime threshold) {
        log.info("Checking Supervisor overdue approvals...");

        // Collect all pending items for each supervisor
        Map<Long, PendingApprovalSummary> supervisorPendings = new HashMap<>();

        // Training Plans waiting for SV
        List<TrainingPlan> pendingPlans = trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(TrainingPlanStatus.WAITING_SV.toString(), threshold);

        for (TrainingPlan plan : pendingPlans) {
            Long svId = plan.getGroup().getSupervisor().getId();
            supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                    .addPlan(plan);
        }

        // Training Results waiting for SV
        List<TrainingResult> pendingResults = trainingResultRepository
                .findByStatusAndUpdatedAtBefore(TrainingResultStatus.ON_GOING.toString(), threshold);

        for (TrainingResult result : pendingResults) {
            Long svId = result.getGroup().getSupervisor().getId();
            supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                    .addResult(result);
        }

        // Issue Reports waiting for SV
        List<IssueReport> pendingIssues = issueReportRepository
                .findByStatusAndUpdatedAtBefore(IssueReportStatus.WAITING_SV.toString(), threshold);

        for (IssueReport issue : pendingIssues) {
            // Cần xác định SV từ issue - có thể thông qua created_by user's group
            Long svId = findSupervisorForIssue(issue);
            if (svId != null) {
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addIssue(issue);
            }
        }

        // Training Topics waiting for SV
        List<TrainingTopic> pendingTopics = trainingTopicRepository
                .findByStatusAndUpdatedAtBefore(TrainingTopicStatus.WAITING_SV.toString(), threshold);

        for (TrainingTopic topic : pendingTopics) {
            Long svId = findSupervisorForTopic(topic);
            if (svId != null) {
                supervisorPendings.computeIfAbsent(svId, k -> new PendingApprovalSummary())
                        .addTopic(topic);
            }
        }

        // Send notifications
        for (Map.Entry<Long, PendingApprovalSummary> entry : supervisorPendings.entrySet()) {
            Long supervisorId = entry.getKey();
            PendingApprovalSummary summary = entry.getValue();

            if (summary.getTotalCount() > 0) {
                sendOverdueReminderToSupervisor(supervisorId, summary);
            }
        }

        log.info("Found {} supervisors with overdue approvals", supervisorPendings.size());
    }

    // ============================================
    // Check Manager Overdue Approvals
    // ============================================

    private void checkManagerOverdueApprovals(LocalDateTime threshold) {
        log.info("Checking Manager overdue approvals...");

        Map<Long, PendingApprovalSummary> managerPendings = new HashMap<>();

        // Training Plans waiting for Manager
        List<TrainingPlan> pendingPlans = trainingPlanRepository
                .findByStatusAndUpdatedAtBefore(TrainingPlanStatus.WAITING_MANAGER.toString(), threshold);

        for (TrainingPlan plan : pendingPlans) {
            Long managerId = plan.getGroup().getSection().getManager().getId();
            managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                    .addPlan(plan);
        }

        // Training Results waiting for Manager
        List<TrainingResult> pendingResults = trainingResultRepository
                .findByStatusAndUpdatedAtBefore(TrainingResultStatus.WAITING_MANAGER.toString(), threshold);

        for (TrainingResult result : pendingResults) {
            Long managerId = result.getGroup().getSection().getManager().getId();
            managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                    .addResult(result);
        }

        // Issue Reports waiting for Manager
        List<IssueReport> pendingIssues = issueReportRepository
                .findByStatusAndUpdatedAtBefore(IssueReportStatus.WAITING_MANAGER.toString(), threshold);

        for (IssueReport issue : pendingIssues) {
            Long managerId = findManagerForIssue(issue);
            if (managerId != null) {
                managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                        .addIssue(issue);
            }
        }

        // Training Topics waiting for Manager
        List<TrainingTopic> pendingTopics = trainingTopicRepository
                .findByStatusAndUpdatedAtBefore(TrainingTopicStatus.WAITING_MANAGER.toString(), threshold);

        for (TrainingTopic topic : pendingTopics) {
            Long managerId = findManagerForTopic(topic);
            if (managerId != null) {
                managerPendings.computeIfAbsent(managerId, k -> new PendingApprovalSummary())
                        .addTopic(topic);
            }
        }

        // Send notifications
        for (Map.Entry<Long, PendingApprovalSummary> entry : managerPendings.entrySet()) {
            Long managerId = entry.getKey();
            PendingApprovalSummary summary = entry.getValue();

            if (summary.getTotalCount() > 0) {
                sendOverdueReminderToManager(managerId, summary);
            }
        }

        log.info("Found {} managers with overdue approvals", managerPendings.size());
    }

    // ============================================
    // Check FI Overdue Confirmations
    // ============================================

    private void checkFIOverdueConfirmations(LocalDateTime threshold) {
        log.info("Checking FI overdue confirmations...");

        Map<Long, List<TrainingResultDetail>> fiPendings = new HashMap<>();

        // Training Result Details waiting for FI
        List<TrainingResultDetail> pendingDetails = trainingResultDetailRepository
                .findByStatusAndUpdatedAtBefore(TrainingResultDetailStatus.WAITING_FI.toString(), threshold);

        for (TrainingResultDetail detail : pendingDetails) {
            // FI user có thể được xác định từ group hoặc được assign
            Long fiUserId = findFIUserForResultDetail(detail);
            if (fiUserId != null) {
                fiPendings.computeIfAbsent(fiUserId, k -> new ArrayList<>())
                        .add(detail);
            }
        }

        // Send notifications
        for (Map.Entry<Long, List<TrainingResultDetail>> entry : fiPendings.entrySet()) {
            Long fiUserId = entry.getKey();
            List<TrainingResultDetail> details = entry.getValue();

            if (!details.isEmpty()) {
                sendOverdueReminderToFI(fiUserId, details);
            }
        }

        log.info("Found {} FI users with overdue confirmations", fiPendings.size());
    }

    // ============================================
    // Send Notification Methods
    // ============================================

    private void sendOverdueReminderToSupervisor(Long supervisorId, PendingApprovalSummary summary) {
        User supervisor = userRepository.findById(supervisorId).orElse(null);
        if (supervisor == null || !supervisor.getIsActive()) {
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", supervisor.getFullName());
        variables.put("pendingCount", summary.getTotalCount());
        variables.put("pendingPlansCount", summary.getPlans().size());
        variables.put("pendingResultsCount", summary.getResults().size());
        variables.put("pendingIssuesCount", summary.getIssues().size());
        variables.put("pendingTopicsCount", summary.getTopics().size());
        variables.put("overdueHours", overdueHours);
        variables.put("approvalLink", "http://localhost:3000/approvals? role=supervisor");

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.APPROVAL_OVERDUE_SV)
                .recipientUserId(supervisor.getId())
                .recipientEmail(supervisor.getEmail())
                .recipientName(supervisor.getFullName())
                .variables(variables)
                .channel(NotificationChannel.EMAIL)
                .priority(8)
                .build();

        notificationService.sendNotification(request);

        log.info("Sent overdue reminder to SV {} for {} pending approvals",
                supervisor.getUsername(), summary.getTotalCount());
    }

    private void sendOverdueReminderToManager(Long managerId, PendingApprovalSummary summary) {
        User manager = userRepository.findById(managerId).orElse(null);
        if (manager == null || !manager.getIsActive()) {
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", manager.getFullName());
        variables.put("pendingCount", summary.getTotalCount());
        variables.put("pendingPlansCount", summary.getPlans().size());
        variables.put("pendingResultsCount", summary.getResults().size());
        variables.put("pendingIssuesCount", summary.getIssues().size());
        variables.put("pendingTopicsCount", summary.getTopics().size());
        variables.put("overdueHours", overdueHours);
        variables.put("approvalLink", "http://localhost:3000/approvals?role=manager");

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.APPROVAL_OVERDUE_MANAGER)
                .recipientUserId(manager.getId())
                .recipientEmail(manager.getEmail())
                .recipientName(manager.getFullName())
                .variables(variables)
                .channel(NotificationChannel.EMAIL)
                .priority(8)
                .build();

        notificationService.sendNotification(request);

        log.info("Sent overdue reminder to Manager {} for {} pending approvals",
                manager.getUsername(), summary.getTotalCount());
    }

    private void sendOverdueReminderToFI(Long fiUserId, List<TrainingResultDetail> details) {
        User fiUser = userRepository.findById(fiUserId).orElse(null);
        if (fiUser == null || !fiUser.getIsActive()) {
            return;
        }

        List<Map<String, Object>> pendingItems = details.stream()
                .map(detail -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("employeeName", detail.getTrainingPlanDetail().getEmployee().getFullName());
                    item.put("processName", detail.getTrainingPlanDetail().getProcess().getName());
                    item.put("actualDate", detail.getActualDate());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", fiUser.getFullName());
        variables.put("pendingCount", details.size());
        variables.put("pendingItems", pendingItems);
        variables.put("overdueHours", overdueHours);
        variables.put("confirmationLink", "http://localhost:3000/results?filter=pending-fi");

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.APPROVAL_OVERDUE_FI)
                .recipientUserId(fiUser.getId())
                .recipientEmail(fiUser.getEmail())
                .recipientName(fiUser.getFullName())
                .variables(variables)
                .channel(NotificationChannel.EMAIL)
                .priority(8)
                .build();

        notificationService.sendNotification(request);

        log.info("Sent overdue reminder to FI {} for {} pending confirmations",
                fiUser.getUsername(), details.size());
    }

    // ============================================
    // Helper Methods
    // ============================================

    private Long findSupervisorForIssue(IssueReport issue) {
        // Logic để tìm Supervisor từ issue
        // Có thể thông qua created_by user -> team -> group -> supervisor
        return null; // TODO: Implement based on your data structure
    }

    private Long findManagerForIssue(IssueReport issue) {
        // Logic để tìm Manager từ issue
        return null; // TODO: Implement
    }

    private Long findSupervisorForTopic(TrainingTopic topic) {
        // Logic để tìm Supervisor từ topic
        return null; // TODO: Implement
    }

    private Long findManagerForTopic(TrainingTopic topic) {
        // Logic để tìm Manager từ topic
        return null; // TODO: Implement
    }

    private Long findFIUserForResultDetail(TrainingResultDetail detail) {
        // Logic để tìm FI user cho result detail
        // Có thể là user có role FINAL_INSPECTION trong cùng group
        return null; // TODO: Implement
    }

    // ============================================
    // Inner class for summary
    // ============================================

    private static class PendingApprovalSummary {
        private final List<TrainingPlan> plans = new ArrayList<>();
        private final List<TrainingResult> results = new ArrayList<>();
        private final List<IssueReport> issues = new ArrayList<>();
        private final List<TrainingTopic> topics = new ArrayList<>();

        public void addPlan(TrainingPlan plan) {
            plans.add(plan);
        }

        public void addResult(TrainingResult result) {
            results.add(result);
        }

        public void addIssue(IssueReport issue) {
            issues.add(issue);
        }

        public void addTopic(TrainingTopic topic) {
            topics.add(topic);
        }

        public List<TrainingPlan> getPlans() {
            return plans;
        }

        public List<TrainingResult> getResults() {
            return results;
        }

        public List<IssueReport> getIssues() {
            return issues;
        }

        public List<TrainingTopic> getTopics() {
            return topics;
        }

        public int getTotalCount() {
            return plans.size() + results.size() + issues.size() + topics.size();
        }
    }
}