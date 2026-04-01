package com.sep490.anomaly_training_backend.service.priority.impl;

import com.sep490.anomaly_training_backend.enums.FactoryDayType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.FactoryCalendar;
import com.sep490.anomaly_training_backend.model.FactoryCalendarEntry;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingPlanSpecialDay;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Smart Multi-Pass Training Schedule Generation
 * <p>
 * Khác biệt từ v1:
 * 1. Filter skills: REVOKED → skip, chỉ xử lý PENDING_REVIEW + VALID
 * 2. Capacity progression: min → max (từng bước 1)
 * 3. Smart distribution: Phân tán lịch đều trên toàn kỳ (không đắp ngay đầu)
 * 4. Expiry-aware: Ưu tiên certify skills sắp hết hạn trước
 * <p>
 * Algorithm:
 * FOR capacity = min TO max:
 * FOR pass_round = 0 TO available_days:
 * FOR each employee (by priority):
 * IF employee_day_index % num_days < daily_capacity:
 * ALLOCATE next uncertified skill
 * <p>
 * Result: Lịch phân tán đều, không bị "đắp" ngày đầu
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TrainingPlanScheduleGenerationServiceImpl implements TrainingPlanScheduleGenerationService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDetailRepository trainingPlanDetailRepository;
    private final FactoryCalendarRepository factoryCalendarRepository;
    private final PrioritySnapshotDetailRepository prioritySnapshotDetailRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    @Override
    public TrainingPlan generateOptimalSchedule(Long trainingPlanId, Long prioritySnapshotId, Integer calendarYear) {
        log.info("=== Starting Smart Multi-Pass Schedule Generation ===");
        log.info("Plan: {}, Snapshot: {}, Year: {}", trainingPlanId, prioritySnapshotId, calendarYear);

        // Load entities
        TrainingPlan trainingPlan = trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        List<PrioritySnapshotDetail> priorityDetails =
                prioritySnapshotDetailRepository.findBySnapshotIdOrderByTierOrderAscSortRankAsc(prioritySnapshotId);

        if (priorityDetails.isEmpty()) {
            log.warn("No employees in snapshot");
            return trainingPlan;
        }

        FactoryCalendar calendar = factoryCalendarRepository.findByCalendarYear(calendarYear)
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_CALENDAR_NOT_FOUND));

        // Get working days
        List<FactoryCalendarEntry> availableDays = getAvailableWorkingDays(
                calendar,
                trainingPlan.getStartDate(),
                trainingPlan.getEndDate()
        );

        log.info("Total working days available: {}", availableDays.size());

        int minCapacity = trainingPlan.getMinTrainingPerDay() != null ? trainingPlan.getMinTrainingPerDay() : 1;
        int maxCapacity = trainingPlan.getMaxTrainingPerDay() != null ? trainingPlan.getMaxTrainingPerDay() : 5;

        log.info("Capacity range: {} - {} per day", minCapacity, maxCapacity);

        // Load available skills per employee (exclude REVOKED)
        Map<Long, List<EmployeeSkill>> employeeAvailableSkills = loadAvailableSkillsPerEmployee(priorityDetails);

        // Smart multi-pass allocation
        List<TrainingPlanDetail> allDetails = allocateSmartMultiPass(
                trainingPlan,
                priorityDetails,
                availableDays,
                employeeAvailableSkills,
                minCapacity,
                maxCapacity
        );

        // Save
        trainingPlanDetailRepository.deleteByTrainingPlanId(trainingPlanId);
        trainingPlanDetailRepository.flush();

        allDetails.forEach(detail -> detail.setTrainingPlan(trainingPlan));
        trainingPlanDetailRepository.saveAll(allDetails);

        log.info("=== Generated {} total training details ===", allDetails.size());

        return trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));
    }

    /**
     * Load available skills per employee (batch query, filter REVOKED)
     */
    private Map<Long, List<EmployeeSkill>> loadAvailableSkillsPerEmployee(List<PrioritySnapshotDetail> priorityDetails) {
        // Collect all employee IDs
        List<Long> employeeIds = priorityDetails.stream()
                .map(p -> p.getEmployee().getId())
                .toList();

        // Single batch query — replaces N individual queries
        List<EmployeeSkill> allSkills = employeeSkillRepository.findAvailableSkillsByEmployeeIds(employeeIds);

        // Group by employee ID (already sorted by expiryDate, id in the query)
        Map<Long, List<EmployeeSkill>> result = allSkills.stream()
                .collect(Collectors.groupingBy(
                        skill -> skill.getEmployee().getId(),
                        Collectors.toList()
                ));

        // Ensure all employees have an entry (even if empty)
        for (Long empId : employeeIds) {
            result.putIfAbsent(empId, List.of());
        }

        log.info("Batch loaded skills for {} employees in 1 query (total: {} skills)",
                employeeIds.size(), allSkills.size());

        return result;
    }

    /**
     * Smart Multi-Pass Allocation with even distribution
     * <p>
     * Key difference from naive approach:
     * - Dùng employee_index mapping để phân tán skills trên days
     * - Không xếp hết 1 employee rồi mới tới employee khác
     * - Phân tán đều: Employee1[day1], Employee2[day2], ..., Employee1[day3], ...
     */
    private List<TrainingPlanDetail> allocateSmartMultiPass(
            TrainingPlan trainingPlan,
            List<PrioritySnapshotDetail> priorityDetails,
            List<FactoryCalendarEntry> availableDays,
            Map<Long, List<EmployeeSkill>> employeeAvailableSkills,
            int minCapacity,
            int maxCapacity) {

        List<TrainingPlanDetail> allDetails = new ArrayList<>();
        int totalDays = availableDays.size();

        // Track which skill index per employee in current pass
        Map<Long, Integer> employeeSkillIndex = new HashMap<>();
        priorityDetails.forEach(p -> employeeSkillIndex.put(p.getEmployee().getId(), 0));

        HashMap<LocalDate, Integer> specialDays = new HashMap<>();
        for (TrainingPlanSpecialDay specialDay : trainingPlan.getSpecialDays()) {
            specialDays.put(specialDay.getSpecialDate(), specialDay.getTrainingSlot());
        }

        AtomicLong totalSkills = new AtomicLong();
        employeeAvailableSkills.forEach((k, v) -> {
            totalSkills.addAndGet(v.size());
        });

        // Multi-pass: từng bước tăng capacity
        for (int dayCapacity = minCapacity; dayCapacity <= maxCapacity; dayCapacity++) {
            log.info("\n--- PASS {} (Daily Capacity: {}) ---", dayCapacity - minCapacity + 1, dayCapacity);

            int passDetails = 0;
            boolean hasAllocatedInPass = false;

            // Track employees via round-robin across days
            int employeeStartIndex = 0;

            for (int dayIndex = 0; dayIndex < totalDays && totalSkills.get() > 0; dayIndex++) {
                FactoryCalendarEntry calendarDay = availableDays.get(dayIndex);
                LocalDate workDate = calendarDay.getWorkDate();

                // Tính số slots hôm này
                int slotsToday = 1;
                if (specialDays.containsKey(workDate)) {
                    slotsToday = specialDays.get(workDate);
                }

                log.debug("  Day {}: {} (slots: {})", dayIndex + 1, workDate, slotsToday);

                // Allocate for this day: try every employee starting from round-robin position
                int allocated = 0;
                int tried = 0;
                int emplIdx = employeeStartIndex;

                while (allocated < slotsToday && tried < priorityDetails.size() && totalSkills.get() > 0) {
                    PrioritySnapshotDetail priority = priorityDetails.get(emplIdx);
                    Employee employee = priority.getEmployee();
                    Long empId = employee.getId();

                    int skillIdx = employeeSkillIndex.getOrDefault(empId, 0);
                    List<EmployeeSkill> availableSkills = employeeAvailableSkills.get(empId);

                    // Check: có skill nào để allocate không?
                    if (availableSkills != null && skillIdx < availableSkills.size()) {
                        EmployeeSkill skill = availableSkills.get(skillIdx);

                        TrainingPlanDetail detail = TrainingPlanDetail.builder()
                                .trainingPlan(trainingPlan)
                                .employee(employee)
                                .targetMonth(workDate.withDayOfMonth(1))
                                .plannedDate(workDate)
                                .status(ReportStatus.PENDING_REVIEW)
                                .batchId(generateBatchId(trainingPlan, workDate))
                                .build();

                        allDetails.add(detail);
                        employeeSkillIndex.put(empId, skillIdx + 1);
                        allocated++;
                        passDetails++;
                        totalSkills.decrementAndGet();
                        hasAllocatedInPass = true;

                        log.debug("    → {} trained on {} (skill {}/{})",
                                employee.getEmployeeCode(),
                                skill.getProcess().getName(),
                                skillIdx + 1,
                                availableSkills.size());
                    }

                    emplIdx = (emplIdx + 1) % priorityDetails.size();
                    tried++;
                }

                // Advance round-robin start for next day only if we allocated
                if (allocated > 0) {
                    employeeStartIndex = emplIdx;
                }
            }

            log.info("PASS {} allocated: {} details", dayCapacity - minCapacity + 1, passDetails);

            // Stopping condition: không allocate được gì trong pass này
            if (!hasAllocatedInPass) {
                log.info("No allocations in pass {} — stopping", dayCapacity - minCapacity + 1);
                break;
            }
        }

        return allDetails;
    }

    /**
     * Get available working days
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

    private boolean isWorkingDay(FactoryDayType dayType) {
        return dayType == FactoryDayType.WORKING_DAY ||
                dayType == FactoryDayType.MAKEUP_DAY ||
                dayType == FactoryDayType.NIGHT_SHIFT;
    }

    private String generateBatchId(TrainingPlan trainingPlan, LocalDate date) {
        return "BATCH_" + trainingPlan.getId() + "_" + date.toString().replace("-", "");
    }

}