package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.enums.FactoryDayType;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.FactoryCalendar;
import com.sep490.anomaly_training_backend.model.FactoryCalendarEntry;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingPlanSpecialDay;
import com.sep490.anomaly_training_backend.repository.FactoryCalendarEntryRepository;
import com.sep490.anomaly_training_backend.repository.FactoryCalendarRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.service.priority.TrainingPlanScheduleGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service để generate tối ưu lịch huấn luyện
 * <p>
 * Quy trình:
 * 1. Load Priority Snapshot → list employees ranked by priority + tiers
 * 2. Load Factory Calendar → available working days
 * 3. Load Training Plan config → min/max slots/ngày, special days
 * 4. Allocate training slots cho employees dựa trên:
 * - Priority tier (Tier 1 → sớm hơn)
 * - Available capacity (min-max per day)
 * - Factory calendar (skip holidays, special days)
 * 5. Save TrainingPlanDetails
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TrainingPlanScheduleGenerationServiceImpl implements TrainingPlanScheduleGenerationService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDetailRepository trainingPlanDetailRepository;
    private final FactoryCalendarRepository factoryCalendarRepository;
    private final FactoryCalendarEntryRepository factoryCalendarEntryRepository;
    private final PrioritySnapshotDetailRepository prioritySnapshotDetailRepository;

    /**
     * Generate optimal training schedule
     *
     * @param trainingPlanId     Training plan ID (có chứa min/max per day, special days)
     * @param prioritySnapshotId Priority snapshot ID (danh sách employees ranked)
     * @param calendarYear       Năm lịch factory calendar
     * @return Generated training plan details
     */
    @Override
    public TrainingPlan generateOptimalSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear) {
        log.info("Starting optimal schedule generation for plan: {}, snapshot: {}, year: {}",
                trainingPlanId, prioritySnapshotId, calendarYear);

        // 1. Load training plan
        TrainingPlan trainingPlan = trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND,
                        "Training plan not found: " + trainingPlanId));

        // 2. Load priority snapshot details (employees ranked by priority)
        List<PrioritySnapshotDetail> priorityDetails =
                prioritySnapshotDetailRepository.findBySnapshotIdOrderByTierOrderAscSortRankAsc(prioritySnapshotId);

        if (priorityDetails.isEmpty()) {
            throw new AppException(ErrorCode.PRIORITY_SNAPSHOT_NOT_FOUND,
                    "No employees in priority snapshot: " + prioritySnapshotId);
        }

        // 3. Load factory calendar
        FactoryCalendar calendar = factoryCalendarRepository.findByCalendarYear(calendarYear)
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_CALENDAR_NOT_FOUND,
                        "Factory calendar not found for year: " + calendarYear));

        // 4. Get available working days from factory calendar
        List<FactoryCalendarEntry> availableDays = getAvailableWorkingDays(
                calendar,
                trainingPlan.getStartDate(),
                trainingPlan.getEndDate()
        );

        log.info("Found {} available working days", availableDays.size());

        // 5. Get special training slots from training plan
        List<LocalDate> specialDays = trainingPlan.getSpecialDays().stream()
                .map(TrainingPlanSpecialDay::getSpecialDate)
                .collect(Collectors.toList());

        // 6. Generate schedule using greedy allocation algorithm
        List<TrainingPlanDetail> details = allocateTrainingSlots(
                trainingPlan,
                priorityDetails,
                availableDays,
                specialDays
        );

        // 7. Save details
        trainingPlan.setDetails(details);
        TrainingPlan saved = trainingPlanRepository.save(trainingPlan);

        log.info("Generated {} training plan details", details.size());
        return saved;
    }

    /**
     * Get available working days from factory calendar
     * (exclude holidays, weekends, off days)
     */
    private List<FactoryCalendarEntry> getAvailableWorkingDays(
            FactoryCalendar calendar,
            LocalDate startDate,
            LocalDate endDate) {

        return calendar.getEntries().stream()
                .filter(entry -> !entry.getWorkDate().isBefore(startDate))
                .filter(entry -> !entry.getWorkDate().isAfter(endDate))
                .filter(entry -> isWorkingDay(entry.getDayType()))
                .sorted(Comparator.comparing(FactoryCalendarEntry::getWorkDate))
                .collect(Collectors.toList());
    }

    /**
     * Check if day type is suitable for training
     */
    private boolean isWorkingDay(FactoryDayType dayType) {
        return dayType == FactoryDayType.WORKING_DAY ||
                dayType == FactoryDayType.MAKEUP_DAY ||
                dayType == FactoryDayType.NIGHT_SHIFT;
    }

    /**
     * Core algorithm: Greedy allocation của training slots
     * <p>
     * Logic:
     * 1. Sort employees theo priority (tier order + sort rank)
     * 2. Duyệt từng ngày theo lịch calendar
     * 3. Mỗi ngày allocate min-max training slots
     * 4. Assign employees từ priority list (theo thứ tự)
     * 5. Nếu employee không thể schedule (no more slots), move to next day
     */
    private List<TrainingPlanDetail> allocateTrainingSlots(
            TrainingPlan trainingPlan,
            List<PrioritySnapshotDetail> priorityDetails,
            List<FactoryCalendarEntry> availableDays,
            List<LocalDate> specialDays) {

        List<TrainingPlanDetail> details = new ArrayList<>();

        int minPerDay = trainingPlan.getMinTrainingPerDay() != null ? trainingPlan.getMinTrainingPerDay() : 1;
        int maxPerDay = trainingPlan.getMaxTrainingPerDay() != null ? trainingPlan.getMaxTrainingPerDay() : 5;

        // Track allocated count per employee (để không schedule 1 employee 2 lần)
        Map<Long, Integer> allocatedPerEmployee = new java.util.HashMap<>();

        // Pointer để duyệt danh sách employees
        int employeeIndex = 0;

        for (FactoryCalendarEntry calendarDay : availableDays) {
            LocalDate workDate = calendarDay.getWorkDate();

            // Tính số slots có thể allocate hôm nay
            int slotsToday = calculateSlotsForDay(workDate, specialDays, maxPerDay);

            log.debug("Day: {}, Slots available: {}", workDate, slotsToday);

            // Allocate training cho employees hôm này
            int allocated = 0;
            while (allocated < slotsToday && employeeIndex < priorityDetails.size()) {
                PrioritySnapshotDetail priority = priorityDetails.get(employeeIndex);
                Employee employee = priority.getEmployee();

                // Check: employee đã được schedule chưa?
                if (allocatedPerEmployee.getOrDefault(employee.getId(), 0) > 0) {
                    // Skip - employee này đã được schedule rồi
                    employeeIndex++;
                    continue;
                }

                // Create training plan detail
                TrainingPlanDetail detail = TrainingPlanDetail.builder()
                        .trainingPlan(trainingPlan)
                        .employee(employee)
                        .targetMonth(workDate.withDayOfMonth(1))  // First day of month
                        .plannedDate(workDate)
                        .status(TrainingPlanDetailStatus.PENDING)
                        .batchId(generateBatchId(trainingPlan, workDate))
                        .note(String.format("Tier %d, Rank %d", priority.getTierOrder(), priority.getSortRank()))
                        .build();

                details.add(detail);
                allocatedPerEmployee.put(employee.getId(), 1);
                allocated++;
                employeeIndex++;
            }

            log.debug("Day: {}, Allocated: {}/{}", workDate, allocated, slotsToday);

            // Early exit: tất cả employees đã được schedule
            if (employeeIndex >= priorityDetails.size()) {
                break;
            }
        }

        // Warn if không schedule được hết
        int notScheduled = priorityDetails.size() - allocatedPerEmployee.size();
        if (notScheduled > 0) {
            log.warn("Could not schedule {} employees due to insufficient slots", notScheduled);
        }

        return details;
    }

    /**
     * Calculate training slots for a specific day
     * <p>
     * Logic:
     * - Normal working day: use maxPerDay
     * - Special day: use special day slot if defined
     * - Reduce if it's last days of plan (handle remaining employees)
     */
    private int calculateSlotsForDay(LocalDate date, List<LocalDate> specialDays, int maxPerDay) {
        // Check if it's a special day with defined slots
        if (specialDays.contains(date)) {
            // TODO: Get special day slot count from training plan
            return maxPerDay * 2;  // Double capacity on special days
        }

        return maxPerDay;
    }

    /**
     * Generate batch ID để identify employees added on same day/batch
     */
    private String generateBatchId(TrainingPlan trainingPlan, LocalDate date) {
        return "BATCH_" + trainingPlan.getId() + "_" + date.toString().replace("-", "");
    }

    /**
     * Recalculate/regenerate schedule (xóa old, tạo new)
     */
    public TrainingPlan regenerateSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear) {
        log.info("Regenerating schedule for plan: {}", trainingPlanId);

        TrainingPlan trainingPlan = trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND,
                        "Training plan not found: " + trainingPlanId));

        // Delete old details
        trainingPlanDetailRepository.deleteByTrainingPlanId(trainingPlanId);

        // Generate new schedule
        return generateOptimalSchedule(trainingPlanId, prioritySnapshotId, calendarYear);
    }

    /**
     * Get available slots for specific date
     * (useful for API to check availability)
     */
    public int getAvailableSlots(Long trainingPlanId, LocalDate date) {
        TrainingPlan trainingPlan = trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        // Get allocated count for this date
        int allocated = trainingPlanDetailRepository.countByTrainingPlanIdAndPlannedDate(trainingPlanId, date);

        int maxPerDay = trainingPlan.getMaxTrainingPerDay() != null ? trainingPlan.getMaxTrainingPerDay() : 5;

        return Math.max(0, maxPerDay - allocated);
    }

    /**
     * Get schedule summary
     */
    public ScheduleSummary getScheduleSummary(Long trainingPlanId) {
        TrainingPlan trainingPlan = trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        List<TrainingPlanDetail> details = trainingPlan.getDetails();

        // Group by date
        Map<LocalDate, Long> countByDate = details.stream()
                .collect(Collectors.groupingBy(
                        TrainingPlanDetail::getPlannedDate,
                        Collectors.counting()
                ));

        // Group by status
        Map<TrainingPlanDetailStatus, Long> countByStatus = details.stream()
                .collect(Collectors.groupingBy(
                        TrainingPlanDetail::getStatus,
                        Collectors.counting()
                ));

        return ScheduleSummary.builder()
                .totalSlots(details.size())
                .totalDays(countByDate.size())
                .avgSlotsPerDay((double) details.size() / Math.max(1, countByDate.size()))
                .countByDate(countByDate)
                .countByStatus(countByStatus)
                .build();
    }
}