package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.ScheduleRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanGenerationRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeePlanGroup;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanGenerationResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.dto.scoring.PrioritySnapshotResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PrioritySnapshotMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.PriorityPolicy;
import com.sep490.anomaly_training_backend.model.PrioritySnapshot;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetailHistory;
import com.sep490.anomaly_training_backend.model.TrainingPlanHistory;
import com.sep490.anomaly_training_backend.model.TrainingPlanSpecialDay;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.PriorityPolicyRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotDetailRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanHistoryRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.priority.TrainingPlanScheduleGenerationService;
import com.sep490.anomaly_training_backend.service.priority.impl.PriorityScoringServiceImpl;
import com.sep490.anomaly_training_backend.util.ReportUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDetailRepository trainingPlanDetailRepository;
    private final TrainingPlanMapper planMapper;
    private final PrioritySnapshotMapper prioritySnapshotMapper;
    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TrainingPlanHistoryRepository trainingPlanHistoryRepository;
    private final ApprovalService approvalService;
    private final TrainingResultService trainingResultService;
    private final ProductLineRepository productLineRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final TrainingResultRepository trainingResultRepository;
    private final PriorityScoringServiceImpl priorityScoringService;
    private final PriorityPolicyRepository policyRepository;
    private final PrioritySnapshotRepository prioritySnapshotRepository;
    private final PrioritySnapshotDetailRepository prioritySnapshotDetailRepository;
    private final TrainingPlanScheduleGenerationService trainingPlanScheduleGenerationService;
    private final GroupRepository groupRepository;
    private final RejectReasonRepository rejectReasonRepository;
    private final RequiredActionRepository requiredActionRepository;

    @Override
    public List<GroupResponse> getMyManagedGroups() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Team> managedTeams = teamRepository.findAllByTeamLeaderId(currentUser.getId());

        return managedTeams.stream()
                .map(Team::getGroup)
                .distinct()
                .map(group -> new GroupResponse(group.getId(), group.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public TrainingPlanGenerationResponse getPlanDetail(Long id) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));
        return toGenerationResponse(plan);
    }

    @Override
    public List<TrainingPlanGenerationResponse> getAllPlans(User currentUser, Long lineId) {

        // 1. Role: FINAL_INSPECTION
        if (currentUser.hasRole("ROLE_FINAL_INSPECTION")) {
            List<Team> teams = teamRepository.findByFinalInspectionId(currentUser.getId());
            if (teams.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> groupIds = teams.stream().map(team -> team.getGroup().getId()).distinct().toList();

            if (lineId != null) {
                return trainingPlanRepository.findAllByGroupIdsAndLineIdAndDeleteFlagFalse(groupIds, lineId).stream()
                        .map(this::toGenerationResponse)
                        .collect(Collectors.toList());
            } else {
                return trainingPlanRepository.findAllByGroupIdsAndDeleteFlagFalse(groupIds).stream()
                        .map(this::toGenerationResponse)
                        .collect(Collectors.toList());
            }
        }

        // Trạng thái loại trừ cho Manager
        List<ReportStatus> excludedStatuses = Arrays.asList(ReportStatus.DRAFT, ReportStatus.REVISE);

        // 2. Role: MANAGER
        if (currentUser.hasRole("ROLE_MANAGER")) {
            if (lineId != null) {
                return trainingPlanRepository.findAllByManagerAndLineIdAndDeleteFlagFalse(currentUser.getId(), lineId, excludedStatuses).stream()
                        .map(this::toGenerationResponse)
                        .collect(Collectors.toList());
            } else {
                return trainingPlanRepository.findAllByManagerAndDeleteFlagFalse(currentUser.getId(), excludedStatuses).stream()
                        .map(this::toGenerationResponse)
                        .collect(Collectors.toList());
            }
        }

        // 3. Role: SUPERVISOR
        if (currentUser.hasRole("ROLE_SUPERVISOR")) {
            List<Group> groups = groupRepository.findBySupervisorId(currentUser.getId());
            if (groups.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> groupIds = groups.stream().map(com.sep490.anomaly_training_backend.model.Group::getId).distinct().toList();

            if (lineId != null) {
                return trainingPlanRepository.findAllByGroupIdsAndLineIdAndDeleteFlagFalse(groupIds, lineId).stream()
                        .map(this::toGenerationResponse)
                        .collect(Collectors.toList());
            } else {
                return trainingPlanRepository.findAllByGroupIdsAndDeleteFlagFalse(groupIds).stream()
                        .map(this::toGenerationResponse)
                        .collect(Collectors.toList());
            }
        }

        // 4. Role mặc định (Người tạo)
        if (lineId != null) {
            return trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(currentUser.getUsername(), lineId).stream()
                    .map(this::toGenerationResponse)
                    .collect(Collectors.toList());
        } else {
            return trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(currentUser.getUsername()).stream()
                    .map(this::toGenerationResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<TrainingPlanGenerationResponse> getRejectedPlans() {
        List<ReportStatus> rejectedStatuses = List.of(
                ReportStatus.REVISE,
                ReportStatus.REJECTED_BY_SV,
                ReportStatus.REJECTED_BY_MANAGER
        );
        return trainingPlanRepository.findByStatusInAndDeleteFlagFalse(rejectedStatuses).stream()
                .map(this::toGenerationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessResponse> getProcessesByProductLine(Long productLineId) {
        List<Process> processes = processRepository.findByProductLineIdAndDeleteFlagFalse(productLineId);
        return processes.stream().map(p -> {
            ProcessResponse res = new ProcessResponse(p.getId(), p.getCode(), p.getName());
            res.setProductLineId(productLineId);
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductLineResponse> getProductLinesByGroupId(Long groupId) {
        List<ProductLine> productLines = productLineRepository.findByGroupIdAndDeleteFlagFalse(groupId);
        return productLines.stream().map(pl -> ProductLineResponse.builder()
                .id(pl.getId())
                .groupId(pl.getGroup().getId())
                .name(pl.getName())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrainingPlanDetailResponse addDetail(Long planId, TrainingPlanDetailRequest request) {
        // 1. Load plan
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        // 2. Chỉ cho thêm khi DRAFT hoặc REJECTED
        if (plan.getStatus() == ReportStatus.WAITING_SV || plan.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        // 3. Validate Employee
        Employee employee = getValidatedEmployee(request.getEmployeeId());

        // 4. Tạo detail rows cho từng schedule
        List<TrainingPlanDetail> addedDetails = new ArrayList<>();
        String batchId = java.util.UUID.randomUUID().toString();
        if (request.getSchedules() != null) {
            for (ScheduleRequest schedule : request.getSchedules()) {
                if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                    TrainingPlanDetail detail = createBaseDetail(plan, employee, request.getNote(), schedule);
                    detail.setStatus(TrainingPlanDetailStatus.PENDING);
                    detail.setBatchId(batchId);
                    plan.getDetails().add(detail);
                    addedDetails.add(detail);
                }
            }
        }

        if (addedDetails.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_SCHEDULE);
        }

        trainingPlanRepository.save(plan);

        // Trả về detail đầu tiên kèm employeeProcesses
        TrainingPlanDetail firstAdded = addedDetails.get(0);
        TrainingPlanDetailResponse response = planMapper.toDetailResponse(firstAdded);
        populateDetailProcesses(response, plan);
        return response;
    }

    @Override
    @Transactional
    public TrainingPlanDetailResponse updateDetail(Long planId, Long detailId, TrainingPlanDetailRequest request) {
        // 1. Load plan
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        // 2. Validate trạng thái
        if (plan.getStatus() == ReportStatus.WAITING_SV || plan.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        // 3. Tìm detail
        TrainingPlanDetail detail = plan.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND));

        // 4. Validate & update Employee nếu thay đổi
        if (request.getEmployeeId() != null) {
            Employee employee = getValidatedEmployee(request.getEmployeeId());
            detail.setEmployee(employee);
        }

        // 5. Validate & update Process nếu thay đổi
        Long currentProductLineId = (plan.getLine() != null) ? plan.getLine().getId() : null;


        // 6. Update note
        if (request.getNote() != null) {
            detail.setNote(request.getNote());
        }

        // 7. Update schedule nếu có (lấy schedule đầu tiên để update ngày)
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            ScheduleRequest schedule = request.getSchedules().get(0);
            LocalDate targetMonth = schedule.getTargetMonth().withDayOfMonth(1);
            detail.setTargetMonth(targetMonth);
            try {
                LocalDate plannedDate = targetMonth.withDayOfMonth(schedule.getPlannedDay());
                detail.setPlannedDate(plannedDate);
            } catch (DateTimeException e) {
                throw new AppException(ErrorCode.INVALID_DAY_OF_MONTH);
            }
        }

        trainingPlanRepository.save(plan);
        TrainingPlanDetailResponse response = planMapper.toDetailResponse(detail);
        populateDetailProcesses(response, plan);
        return response;
    }

    @Override
    public List<PrioritizedEmployeeResponse> getEmployeesNotInPlan(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        Long groupId = plan.getLine() != null && plan.getLine().getGroup() != null
                ? plan.getLine().getGroup().getId()
                : (plan.getTeam() != null && plan.getTeam().getGroup() != null
                ? plan.getTeam().getGroup().getId() : null);

        if (groupId == null) return List.of();

        List<Employee> allEmployees = employeeRepository.findAllActiveByGroupId(groupId);
        Set<Long> inPlanIds = new java.util.HashSet<>(
                trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(planId));

        Map<Long, PrioritySnapshotDetail> snapshotMap = loadSnapshotMap(planId);
        Map<Long, TrainingResultDetail> lastTrainingMap = loadLastTrainingMap(
                allEmployees.stream().map(Employee::getId).collect(Collectors.toList()));

        return allEmployees.stream()
                .filter(emp -> !inPlanIds.contains(emp.getId()))
                .map(emp -> buildPrioritizedEmployeeResponsee(emp, snapshotMap, lastTrainingMap, inPlanIds))
                .collect(Collectors.toList());
    }

    @Override
    public List<PrioritizedEmployeeResponse> getEmployeesInTeams(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        Long teamId = plan.getTeam() != null ? plan.getTeam().getId() : null;
        if (teamId == null) return List.of();

        List<Employee> allEmployees = employeeRepository.findAllActiveByTeamId(teamId);

        Set<Long> inPlanIds = new java.util.HashSet<>(
                trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(planId));

        Map<Long, PrioritySnapshotDetail> snapshotMap = loadSnapshotMap(planId);

        Map<Long, TrainingResultDetail> lastTrainingMap = loadLastTrainingMap(
                allEmployees.stream().map(Employee::getId).collect(Collectors.toList()));

        return allEmployees.stream()
                .map(emp -> buildPrioritizedEmployeeResponsee(emp, snapshotMap, lastTrainingMap, inPlanIds))
                .collect(Collectors.toList());
    }

    @Override
    public TrainingPlanGenerationResponse generateTrainingPlans(User currentUser, TrainingPlanGenerationRequest request) {
        TrainingPlan generatedTrainingPlan = generateTrainingPlan(request);

        List<Employee> teamMembers = employeeRepository.findAllActiveByTeamId(request.getTeamId());
        PriorityPolicy priorityPolicy = policyRepository.findFirstByEntityTypeAndStatusAndDeleteFlagFalse(PolicyEntityType.EMPLOYEE, PolicyStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.POLICY_NOT_FOUND, "No active priority policy found for employees"));

        PrioritySnapshot prioritySnapshot = priorityScoringService.generateSnapshot(priorityPolicy.getId(), request.getTeamId(), teamMembers);
        prioritySnapshot.setTrainingPlan(generatedTrainingPlan);
        prioritySnapshotRepository.save(prioritySnapshot);

        PrioritySnapshotResponse prioritySnapshotResponse = prioritySnapshotMapper.toResponse(prioritySnapshot);

        TrainingPlanGenerationResponse response = new TrainingPlanGenerationResponse();
        response.setPrioritySnapshot(prioritySnapshotResponse);
        response.setTrainingPlan(
                toTrainingPlanResponse(
                        trainingPlanScheduleGenerationService
                                .generateOptimalSchedule(
                                        generatedTrainingPlan.getId(),
                                        prioritySnapshot.getId(),
                                        LocalDate.now().getYear())));

        return response;
    }

    @Override
    @Transactional
    public TrainingPlanGenerationResponse updatePlan(Long planId, TrainingPlanUpdateRequest request) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (plan.getStatus() == ReportStatus.WAITING_SV || plan.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        boolean isApproved = ReportStatus.APPROVED.equals(plan.getStatus());

        updateHeaderIfPresent(plan, request);

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            processDetailActions(plan, request.getDetails(), isApproved);
        }

        TrainingPlan savedPlan = trainingPlanRepository.save(plan);
        return toGenerationResponse(savedPlan);
    }

    // ==================== HEADER UPDATE ====================

    private void updateHeaderIfPresent(TrainingPlan plan, TrainingPlanUpdateRequest request) {
        if (request.getTitle() != null) {
            plan.setTitle(request.getTitle());
        }
        if (request.getNote() != null) {
            plan.setNote(request.getNote());
        }
        if (request.getStartDate() != null) {
            plan.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            plan.setEndDate(request.getEndDate());
        }

        // Validate date range
        LocalDate start = request.getStartDate() != null ? request.getStartDate() : plan.getStartDate();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : plan.getEndDate();
        if (start != null && end != null && end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (request.getLineId() != null) {
            ProductLine line = productLineRepository.findById(request.getLineId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));
            plan.setLine(line);
        }
    }

    // ==================== DETAIL ACTIONS (1 FLOW CHUNG) ====================

    private void processDetailActions(TrainingPlan plan,
                                      List<TrainingPlanUpdateRequest.DetailAction> actions,
                                      boolean isApproved) {
        Long productLineId = (plan.getLine() != null) ? plan.getLine().getId() : null;

        // Index details hiện tại theo ID → O(1) lookup
        Map<Long, TrainingPlanDetail> detailMap = plan.getDetails().stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(TrainingPlanDetail::getId, d -> d));

        for (TrainingPlanUpdateRequest.DetailAction action : actions) {
            if (action.getAction() == null) {
                throw new AppException(ErrorCode.MISSING_ACTION_IN_DETAIL);
            }

            switch (action.getAction()) {
                case ADD -> handleAddAction(plan, action, productLineId);
                case ADD_SCHEDULE -> handleAddScheduleAction(plan, action);
                case UPDATE -> handleUpdateAction(action, detailMap, productLineId, isApproved);
                case DELETE -> handleDeleteAction(plan, action, detailMap, isApproved);
            }
        }
    }

    /**
     * ADD: Thêm employee vào plan, process tự lấy từ employee_skill
     */
    private void handleAddAction(TrainingPlan plan,
                                 TrainingPlanUpdateRequest.DetailAction action,
                                 Long productLineId) {
        if (action.getEmployeeId() == null) {
            throw new AppException(ErrorCode.MISSING_EMPLOYEE_ID);
        }

        Employee employee = getValidatedEmployee(action.getEmployeeId());

        if (action.getSchedules() == null || action.getSchedules().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_SCHEDULE);
        }

        // Tạo batchId chung cho lần ADD này → phân biệt các lần thêm khác nhau của cùng 1 employee
        String batchId = java.util.UUID.randomUUID().toString();

        for (ScheduleRequest schedule : action.getSchedules()) {
            if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                TrainingPlanDetail detail = createBaseDetail(plan, employee, action.getNote(), schedule);
                detail.setStatus(TrainingPlanDetailStatus.PENDING);
                detail.setBatchId(batchId);
                plan.getDetails().add(detail);
            }
        }
    }

    /**
     * ADD_SCHEDULE: Thêm ngày vào batch cũ (giữ nguyên row trên FE)
     * FE gửi batchId → tìm employee từ batch đó → tạo detail mới cùng batchId
     */
    private void handleAddScheduleAction(TrainingPlan plan,
                                         TrainingPlanUpdateRequest.DetailAction action) {
        if (action.getBatchId() == null || action.getBatchId().isBlank()) {
            throw new AppException(ErrorCode.MISSING_BATCH_ID);
        }

        if (action.getSchedules() == null || action.getSchedules().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_SCHEDULE);
        }

        // Tìm employee từ batch hiện tại (lấy detail đầu tiên cùng batchId)
        Employee employee;
        if (action.getEmployeeId() != null) {
            employee = getValidatedEmployee(action.getEmployeeId());
        } else {
            TrainingPlanDetail existingDetail = plan.getDetails().stream()
                    .filter(d -> action.getBatchId().equals(d.getBatchId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));
            employee = existingDetail.getEmployee();
        }

        // Note: nếu không gửi note → lấy note từ detail cũ cùng batch
        String note = action.getNote();
        if (note == null) {
            note = plan.getDetails().stream()
                    .filter(d -> action.getBatchId().equals(d.getBatchId()))
                    .map(TrainingPlanDetail::getNote)
                    .findFirst()
                    .orElse(null);
        }

        for (ScheduleRequest schedule : action.getSchedules()) {
            if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                TrainingPlanDetail detail = createBaseDetail(plan, employee, note, schedule);
                detail.setStatus(TrainingPlanDetailStatus.PENDING);
                detail.setBatchId(action.getBatchId()); // Giữ nguyên batchId cũ
                plan.getDetails().add(detail);
            }
        }
    }

    /**
     * UPDATE: Sửa 1 detail cụ thể theo detailId — chỉ update field được gửi lên (non-null)
     */
    private void handleUpdateAction(TrainingPlanUpdateRequest.DetailAction action,
                                    Map<Long, TrainingPlanDetail> detailMap,
                                    Long productLineId,
                                    boolean isApproved) {
        if (action.getDetailId() == null) {
            throw new AppException(ErrorCode.MISSING_DETAIL_ID);
        }

        TrainingPlanDetail detail = detailMap.get(action.getDetailId());
        if (detail == null) {
            throw new AppException(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND);
        }

        // Bản APPROVED: không cho sửa detail đã hoàn thành
        if (isApproved && detail.getStatus() == TrainingPlanDetailStatus.DONE) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_COMPLETED_DETAIL);
        }

        if (action.getEmployeeId() != null) {
            detail.setEmployee(getValidatedEmployee(action.getEmployeeId()));
        }
        if (action.getNote() != null) {
            detail.setNote(action.getNote());
        }

        // Update schedule trực tiếp (targetMonth + plannedDay)
        if (action.getTargetMonth() != null && action.getPlannedDay() != null) {
            LocalDate targetMonth = action.getTargetMonth().withDayOfMonth(1);
            detail.setTargetMonth(targetMonth);
            try {
                detail.setPlannedDate(targetMonth.withDayOfMonth(action.getPlannedDay()));
            } catch (DateTimeException e) {
                throw new AppException(ErrorCode.INVALID_DAY_OF_MONTH);
            }
        }

        // Hoặc update qua schedules list (lấy đầu tiên)
        if (action.getSchedules() != null && !action.getSchedules().isEmpty()
                && action.getTargetMonth() == null) {
            ScheduleRequest schedule = action.getSchedules().get(0);
            LocalDate targetMonth = schedule.getTargetMonth().withDayOfMonth(1);
            detail.setTargetMonth(targetMonth);
            try {
                detail.setPlannedDate(targetMonth.withDayOfMonth(schedule.getPlannedDay()));
            } catch (DateTimeException e) {
                throw new AppException(ErrorCode.INVALID_DAY_OF_MONTH);
            }
        }
    }

    /**
     * DELETE: Xóa hoặc đánh dấu MISSED tùy trạng thái plan
     */
    private void handleDeleteAction(TrainingPlan plan,
                                    TrainingPlanUpdateRequest.DetailAction action,
                                    Map<Long, TrainingPlanDetail> detailMap,
                                    boolean isApproved) {
        if (action.getDetailId() == null) {
            throw new AppException(ErrorCode.MISSING_DETAIL_ID);
        }

        TrainingPlanDetail detail = detailMap.get(action.getDetailId());
        if (detail == null) {
            throw new AppException(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND);
        }

        if (isApproved) {
            // APPROVED: không xóa thật, đánh dấu MISSED
            if (detail.getStatus() == TrainingPlanDetailStatus.DONE) {
                throw new AppException(ErrorCode.CANNOT_DELETE_COMPLETED_DETAIL);
            }
            detail.setStatus(TrainingPlanDetailStatus.MISSED);
            detail.setNote("[Đã hủy] " + (detail.getNote() != null ? detail.getNote() : ""));
        } else {
            // DRAFT/REJECTED: xóa training_result_details con trước rồi xóa thật
            trainingResultDetailRepository.deleteByTrainingPlanDetailId(detail.getId());
            plan.getDetails().remove(detail);
        }
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        // Only allow deletion of DRAFT or REJECTED plans
        if (plan.getStatus() != ReportStatus.DRAFT && plan.getStatus() != ReportStatus.REJECTED_BY_MANAGER && plan.getStatus() != ReportStatus.REJECTED_BY_SV) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        // Verify ownership - user must be the creator or have delete permission
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!plan.getCreatedBy().equals(currentUsername)) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Check if user is team leader of the same group
            boolean isTeamLeader = teamRepository.findAllByTeamLeaderId(currentUser.getId())
                    .stream()
                    .anyMatch(team -> team.getId().equals(plan.getTeam().getId()));

            if (!isTeamLeader) {
                throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
            }
        }

        // Xóa training_result_details con trước (FK constraint)
        List<Long> detailIds = plan.getDetails().stream()
                .filter(d -> d.getId() != null)
                .map(TrainingPlanDetail::getId)
                .toList();
        if (!detailIds.isEmpty()) {
            trainingResultDetailRepository.deleteByTrainingPlanDetailIdIn(detailIds);
        }

        trainingPlanRepository.delete(plan);
    }

    @Override
    @Transactional
    public void deleteDetail(Long planId, Long detailId) {
        // 1. Tìm plan
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        // 2. Chỉ cho phép xóa detail khi plan ở trạng thái DRAFT hoặc REJECTED
        if (plan.getStatus() != ReportStatus.DRAFT
                && plan.getStatus() != ReportStatus.REJECTED_BY_MANAGER
                && plan.getStatus() != ReportStatus.REJECTED_BY_SV) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        // 3. Tìm detail trong danh sách
        TrainingPlanDetail detailToRemove = plan.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND));

        if (detailToRemove.getStatus() != TrainingPlanDetailStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }


        // 4. Xóa training_result_details con trước (FK constraint)
        trainingResultDetailRepository.deleteByTrainingPlanDetailId(detailToRemove.getId());

        // 5. Xóa detail khỏi collection (orphanRemoval=true sẽ xóa khỏi DB)
        plan.getDetails().remove(detailToRemove);

        // 6. Lưu plan (cascade sẽ xử lý việc xóa detail)
        trainingPlanRepository.save(plan);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Populate employeeProcesses cho 1 detail response đơn lẻ
     */
    private void populateDetailProcesses(TrainingPlanDetailResponse detailResponse, TrainingPlan plan) {
        Long productLineId = plan.getLine() != null ? plan.getLine().getId() : null;
        if (productLineId == null || detailResponse.getEmployeeId() == null) return;

        List<EmployeeSkill> skills = employeeSkillRepository
                .findSkillsByEmployeeAndLine(detailResponse.getEmployeeId(), productLineId);

        List<TrainingPlanDetailResponse.ProcessInfo> processes = skills.stream()
                .map(skill -> {
                    TrainingPlanDetailResponse.ProcessInfo info = new TrainingPlanDetailResponse.ProcessInfo();
                    info.setId(skill.getProcess().getId());
                    info.setName(skill.getProcess().getName());
                    return info;
                })
                .toList();

        detailResponse.setEmployeeProcesses(processes);
    }

    /**
     * Tạo lại training_result_details cho plan đã APPROVED.
     * Tìm TrainingResult liên quan, tạo result detail cho mỗi plan detail chưa có.
     */
    private void regenerateResultDetails(TrainingPlan plan) {
        List<TrainingResult> results = trainingResultRepository.findByTrainingPlanId(plan.getId());
        if (results.isEmpty()) return;

        TrainingResult result = results.get(0);

        // Lấy set plan detail IDs đã có result detail
        Set<Long> existingPlanDetailIds = result.getDetails().stream()
                .filter(rd -> rd.getTrainingPlanDetail() != null && rd.getTrainingPlanDetail().getId() != null)
                .map(rd -> rd.getTrainingPlanDetail().getId())
                .collect(Collectors.toSet());

        // Tạo result detail cho những plan detail chưa có
        for (TrainingPlanDetail planDetail : plan.getDetails()) {
            if (planDetail.getId() != null && !existingPlanDetailIds.contains(planDetail.getId())) {
                TrainingResultDetail newResultDetail = new TrainingResultDetail();
                newResultDetail.setTrainingResult(result);
                newResultDetail.setTrainingPlanDetail(planDetail);
                newResultDetail.setEmployee(planDetail.getEmployee());
                newResultDetail.setPlannedDate(planDetail.getPlannedDate());
                result.getDetails().add(newResultDetail);
            }
        }

        trainingResultRepository.save(result);
    }

    private Employee getValidatedEmployee(Long employeeId) {
        // Nên dùng getReferenceById nếu chỉ cần gán quan hệ (lazy), nhưng findById an toàn hơn để check tồn tại
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private Process getValidatedProcess(Long processId, Long productLineId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        // Validate Process thuộc ProductLine được chọn trong Plan
        if (productLineId != null && process.getProductLine() != null) {
            if (!process.getProductLine().getId().equals(productLineId)) {
                throw new AppException(ErrorCode.PROCESS_NOT_IN_PRODUCT_LINE);
            }
        }

        return process;
    }

    private TrainingPlanDetail createBaseDetail(TrainingPlan plan, Employee employee, String note, ScheduleRequest schedule) {
        TrainingPlanDetail detailEntity = new TrainingPlanDetail();

        // Gán các quan hệ
        detailEntity.setTrainingPlan(plan);
        detailEntity.setEmployee(employee);

        // Gán thông tin text
        detailEntity.setNote(note);

        // Xử lý ngày tháng
        LocalDate targetMonth = schedule.getTargetMonth().withDayOfMonth(1); // Luôn lấy ngày mùng 1 để chuẩn hóa
        detailEntity.setTargetMonth(targetMonth);

        try {
            // Tạo ngày cụ thể từ ngày mùng 1 + số ngày user chọn
            // Ví dụ: targetMonth = 01/11, plannedDay = 15 => 15/11
            // Nếu user gửi plannedDay = 31 mà tháng chỉ có 30 ngày => Sẽ throw exception
            LocalDate plannedDate = targetMonth.withDayOfMonth(schedule.getPlannedDay());
            detailEntity.setPlannedDate(plannedDate);
        } catch (DateTimeException e) {
            throw new AppException(ErrorCode.INVALID_DAY_OF_MONTH);
        }

        return detailEntity;
    }

    // Relate approval methods
    @Override
    public void submitPlanForApproval(Long planId, User currentUser, HttpServletRequest request) {
        TrainingPlan plan = getReportById(planId);

        validatePlanForSubmission(plan);

        plan.setFormCode(ReportUtils.generateFormCode(ApprovalEntityType.TRAINING_PLAN, plan.getLine().getName(), planId));


        approvalService.submit(plan, currentUser, request);

        trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void submit(Long reportId, User currentUser, HttpServletRequest request) {
        TrainingPlan report = getReportById(reportId);
        approvalService.submit(report, currentUser, request);
        trainingPlanRepository.save(report);
    }

    @Override
    @Transactional
    public void revise(Long reportId, User currentUser, HttpServletRequest request) {
        TrainingPlan report = getReportById(reportId);

        if (!report.getCreatedBy().equals(currentUser.getUsername())) {
            throw new AppException(ErrorCode.ONLY_AUTHOR_CAN_EDIT);
        }
        createHistorySnapshot(report);
        approvalService.revise(report, currentUser, request);
        trainingPlanRepository.save(report);
    }

    @Override
    @Transactional
    public void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        TrainingPlan report = getReportById(reportId);
        approvalService.approve(report, currentUser, req, request);
        if (report.getStatus() == ReportStatus.APPROVED) {
//            createHistorySnapshot(report); // Lưu snapshot trước khi tạo Training Result
            trainingResultService.generateTrainingResult(reportId);
        }
        trainingPlanRepository.save(report);
    }

    @Override
    @Transactional
    public void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request) {
        TrainingPlan report = getReportById(reportId);
        approvalService.reject(report, currentUser, req, request);
        trainingPlanRepository.save(report);
    }

    @Override
    public boolean canApprove(Long reportId, User currentUser) {
        TrainingPlan report = getReportById(reportId);
        return approvalService.canApprove(report, currentUser);
    }

    // ── Save feedback ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void saveFeedback(Long detailId, DetailFeedbackRequest request, User currentUser) {

        TrainingPlanDetail detail = trainingPlanDetailRepository.findByIdAndDeleteFlagFalse(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_DETAIL_NOT_FOUND));

        // Tất cả null/empty → xoá feedback
        if (isEmptyFeedback(request)) {
            detail.setRejectFeedback(null);
            trainingPlanDetailRepository.save(detail);
            return;
        }

        // Batch load reasons + action
        List<RejectFeedbackJson.RejectReasonSnapshot> reasonSnapshots = List.of();
        if (request.getRejectReasonIds() != null && !request.getRejectReasonIds().isEmpty()) {
            reasonSnapshots = rejectReasonRepository
                    .findAllById(request.getRejectReasonIds())
                    .stream()
                    .map(r -> RejectFeedbackJson.RejectReasonSnapshot.builder()
                            .id(r.getId())
                            .category(r.getCategoryName())
                            .label(r.getReasonName())
                            .build())
                    .toList();
        }

        RejectFeedbackJson.RequiredActionSnapshot actionSnapshot = null;
        if (request.getRequiredActionId() != null) {
            actionSnapshot = requiredActionRepository
                    .findById(request.getRequiredActionId())
                    .map(a -> RejectFeedbackJson.RequiredActionSnapshot.builder()
                            .id(a.getId())
                            .label(a.getActionName())
                            .build())
                    .orElse(null);
        }

        detail.setRejectFeedback(RejectFeedbackJson.builder()
                .savedAt(Instant.now())
                .savedBy(currentUser.getFullName())
                .rejectReasons(reasonSnapshots.isEmpty() ? null : reasonSnapshots)
                .requiredAction(actionSnapshot)
                .comment(request.getComment())
                .build());

        trainingPlanDetailRepository.save(detail);
        log.info("[RejectFeedback] detailId={} updated by {}", detailId, currentUser.getUsername());
    }

    private boolean isEmptyFeedback(DetailFeedbackRequest r) {
        return (r.getRejectReasonIds() == null || r.getRejectReasonIds().isEmpty())
                && r.getRequiredActionId() == null
                && (r.getComment() == null || r.getComment().isBlank());
    }

    // ── Clear feedback (khi TL revise lại) ───────────────────────────────────

    @Override
    @Transactional
    public void clearFeedback(Long proposalId) {
        List<TrainingPlanDetail> details =
                trainingPlanDetailRepository.findByTrainingPlanIdAndDeleteFlagFalse(proposalId);

        details.forEach(d -> d.setRejectFeedback(null));
        trainingPlanDetailRepository.saveAll(details);

        log.info("[RejectFeedback] Đã xoá toàn bộ feedback của proposalId={}", proposalId);
    }

    private TrainingPlan getReportById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));
    }

    // private methods
    private void validatePlanForSubmission(TrainingPlan plan) {
        // Business rules specific to TrainingPlan
        if (plan.getDetails() == null || plan.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.PLAN_HAS_NO_DETAILS);
        }

        if (plan.getTitle() == null || plan.getTitle().trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_PLAN_TITLE);
        }

        // Validate date range
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        // Validate details have required fields and business rules
        for (TrainingPlanDetail detail : plan.getDetails()) {
            if (detail.getEmployee() == null) {
                throw new AppException(ErrorCode.MISSING_EMPLOYEE_IN_DETAIL);
            }
//            if (detail.getProcess() == null) {
//                throw new IllegalArgumentException("Detail lack of Process Information.");
//            }
            if (detail.getPlannedDate() == null) {
                throw new AppException(ErrorCode.MISSING_PLANNED_DATE_IN_DETAIL);
            }

            // Validate plannedDate is within plan's date range
            if (detail.getPlannedDate().isBefore(plan.getStartDate()) ||
                    detail.getPlannedDate().isAfter(plan.getEndDate())) {
                throw new AppException(ErrorCode.PLANNED_DATE_OUT_OF_RANGE);
            }
        }

        // Check for duplicates: same employee + date + batchId
        for (int i = 0; i < plan.getDetails().size(); i++) {
            TrainingPlanDetail detail1 = plan.getDetails().get(i);
            for (int j = i + 1; j < plan.getDetails().size(); j++) {
                TrainingPlanDetail detail2 = plan.getDetails().get(j);

                boolean sameEmployee = detail1.getEmployee().getId().equals(detail2.getEmployee().getId());
                boolean sameDate = detail1.getPlannedDate().equals(detail2.getPlannedDate());
                boolean sameBatch = (detail1.getBatchId() != null && detail1.getBatchId().equals(detail2.getBatchId()));

                if (sameEmployee && sameDate && sameBatch) {
                    throw new AppException(ErrorCode.DUPLICATE_TRAINING_SCHEDULE);
                }
            }
        }
    }

    /**
     * Hàm private thực hiện việc copy dữ liệu từ Plan -> History Entity
     */
    private void createHistorySnapshot(TrainingPlan plan) {
        TrainingPlanHistory history = TrainingPlanHistory.builder()
                .trainingPlan(plan)
                .version(plan.getCurrentVersion() == null ? 1 : plan.getCurrentVersion())
                .title(plan.getTitle())
                .formCode(plan.getFormCode())
                .monthStart(plan.getStartDate())
                .monthEnd(plan.getEndDate())
                .note(plan.getNote())
                .recordedAt(LocalDateTime.now())
                .lineId(plan.getLine() != null ? plan.getLine().getId() : null)
                .detailHistories(new ArrayList<>())
                .build();

        // 2. Map Details (TrainingPlanDetail -> TrainingPlanDetailHistory)
        if (plan.getDetails() != null) {
            for (TrainingPlanDetail detail : plan.getDetails()) {

                TrainingPlanDetailHistory detailHistory = TrainingPlanDetailHistory.builder()
                        .trainingPlanHistory(history)
                        .employeeId(detail.getEmployee().getId())
//                        .processId(detail.getProcess().getId())
                        .targetMonth(detail.getTargetMonth())
                        .plannedDate(detail.getPlannedDate())
                        .actualDate(detail.getActualDate())
                        .status(detail.getStatus().toString())
                        .batchId(detail.getBatchId())
                        .note(detail.getNote())
                        .build();

                history.getDetailHistories().add(detailHistory);
            }
        }

        trainingPlanHistoryRepository.save(history);
    }

    private TrainingPlan generateTrainingPlan(TrainingPlanGenerationRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        ProductLine productLine = productLineRepository.findById(request.getLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_LINE_NOT_FOUND));

        TrainingPlan trainingPlan = new TrainingPlan();
        trainingPlan.setTitle(request.getTitle());
        trainingPlan.setStartDate(request.getStartDate());
        trainingPlan.setEndDate(request.getEndDate());
        trainingPlan.setTeam(team);
        trainingPlan.setLine(productLine);
        trainingPlan.setMaxTrainingPerDay(request.getMaxTrainingPerDay());
        trainingPlan.setMinTrainingPerDay(request.getMinTrainingPerDay());

        if (request.getSpecialDays() != null) {
            List<TrainingPlanSpecialDay> specialDays = request.getSpecialDays().stream()
                    .map(day -> {
                        TrainingPlanSpecialDay specialDay = new TrainingPlanSpecialDay();
                        specialDay.setSpecialDate(day.getSpecialDay());
                        specialDay.setTrainingSlot(day.getTrainingSlot());
                        specialDay.setTrainingPlan(trainingPlan);
                        return specialDay;
                    }).toList();
            trainingPlan.setSpecialDays(specialDays);
        }

        return trainingPlanRepository.save(trainingPlan);
    }

    private TrainingPlanResponse toTrainingPlanResponse(TrainingPlan trainingPlan) {
        TrainingPlanResponse response = planMapper.toResponse(trainingPlan);
        List<Employee> teamMembers = employeeRepository.findAllActiveByTeamId(trainingPlan.getTeam().getId());

        List<TrainingPlanDetailResponse> prefilledDetails = new ArrayList<>();

        if (teamMembers != null) {
            for (Employee emp : teamMembers) {
                TrainingPlanDetailResponse detailRes = new TrainingPlanDetailResponse();

                detailRes.setEmployeeId(emp.getId());
                detailRes.setEmployeeCode(emp.getEmployeeCode());
                detailRes.setEmployeeName(emp.getFullName());
                detailRes.setBatchId(java.util.UUID.randomUUID().toString());

                detailRes.setId(null);
                detailRes.setPlannedDate(null);
                detailRes.setStatus(null);
                detailRes.setNote("");

                prefilledDetails.add(detailRes);
            }
        }

        response.setDetails(prefilledDetails);
        populateEmployeeProcesses(response, trainingPlan);

        return response;
    }

    /**
     * Populate employeeProcesses cho từng detail + build groupedDetails (group theo batchId)
     */
    private void populateEmployeeProcesses(TrainingPlanResponse response, TrainingPlan plan) {
        Long productLineId = plan.getLine() != null ? plan.getLine().getId() : null;
        if (response.getDetails() == null || productLineId == null) return;

        // Cache skill lookup để không query trùng employee
        Map<Long, List<TrainingPlanDetailResponse.ProcessInfo>> skillCache = new java.util.HashMap<>();

        for (TrainingPlanDetailResponse detail : response.getDetails()) {
            if (detail.getEmployeeId() != null) {
                List<TrainingPlanDetailResponse.ProcessInfo> processes = skillCache.computeIfAbsent(
                        detail.getEmployeeId(),
                        empId -> {
                            List<EmployeeSkill> skills = employeeSkillRepository
                                    .findSkillsByEmployeeAndLine(empId, productLineId);
                            return skills.stream()
                                    .map(skill -> {
                                        TrainingPlanDetailResponse.ProcessInfo info = new TrainingPlanDetailResponse.ProcessInfo();
                                        info.setId(skill.getProcess().getId());
                                        info.setName(skill.getProcess().getName());
                                        return info;
                                    })
                                    .toList();
                        }
                );
                detail.setEmployeeProcesses(processes);
            }
        }

        // Build groupedDetails: group theo batchId (phân biệt các lần thêm khác nhau của cùng 1 employee)
        Map<String, EmployeePlanGroup> groupMap = new java.util.LinkedHashMap<>();

        for (TrainingPlanDetailResponse detail : response.getDetails()) {
            if (detail.getEmployeeId() == null) continue;

            // Dùng batchId làm key. Nếu batchId null (data cũ chưa có) → fallback dùng "employee_{id}"
            String key = detail.getBatchId() != null
                    ? detail.getBatchId()
                    : "employee_" + detail.getEmployeeId();

            EmployeePlanGroup group = groupMap.computeIfAbsent(key, k -> {
                EmployeePlanGroup g = new EmployeePlanGroup();
                g.setBatchId(detail.getBatchId());
                g.setEmployeeId(detail.getEmployeeId());
                g.setEmployeeName(detail.getEmployeeName());
                g.setEmployeeCode(detail.getEmployeeCode());
                g.setEmployeeProcesses(detail.getEmployeeProcesses());
                return g;
            });

            group.getSchedules().add(detail);
        }

        response.setGroupedDetails(new ArrayList<>(groupMap.values()));
    }

    private TrainingPlanGenerationResponse toGenerationResponse(TrainingPlan plan) {
        TrainingPlanResponse trainingPlanResponse = planMapper.toResponse(plan);
        populateEmployeeProcesses(trainingPlanResponse, plan);

        PrioritySnapshotResponse prioritySnapshotResponse = prioritySnapshotMapper.toResponse(
                prioritySnapshotRepository.findByTrainingPlanId(plan.getId()).orElse(null)
        );

        TrainingPlanGenerationResponse response = new TrainingPlanGenerationResponse();

        User planCreator = userRepository.findByUsername(plan.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        trainingPlanResponse.setCreatedBy(planCreator.getFullName());

        response.setTrainingPlan(trainingPlanResponse);

        response.setPrioritySnapshot(prioritySnapshotResponse);
        return response;
    }

    private Map<Long, PrioritySnapshotDetail> loadSnapshotMap(Long planId) {
        return prioritySnapshotRepository.findByTrainingPlanId(planId)
                .map(snapshot -> prioritySnapshotDetailRepository
                        .findBySnapshotId(snapshot.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                d -> d.getEmployee().getId(),
                                d -> d,
                                (d1, d2) -> d1.getTierOrder() <= d2.getTierOrder() ? d1 : d2
                        )))
                .orElse(Map.of());
    }

    private Map<Long, TrainingResultDetail> loadLastTrainingMap(List<Long> employeeIds) {
        if (employeeIds.isEmpty()) return Map.of();
        return trainingResultDetailRepository
                .findLatestByEmployeeIds(employeeIds)
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getEmployee().getId(),
                        d -> d,
                        (d1, d2) -> d1.getActualDate().isAfter(d2.getActualDate()) ? d1 : d2
                ));
    }

    private PrioritizedEmployeeResponse buildPrioritizedEmployeeResponsee(
            Employee emp,
            Map<Long, PrioritySnapshotDetail> snapshotMap,
            Map<Long, TrainingResultDetail> lastTrainingMap,
            Set<Long> inPlanIds) {

        PrioritySnapshotDetail snapshot = snapshotMap.get(emp.getId());
        TrainingResultDetail lastTraining = lastTrainingMap.get(emp.getId());

        return PrioritizedEmployeeResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getFullName())
                .status(emp.getStatus())
                .teamId(emp.getTeam() != null ? emp.getTeam().getId() : null)
                .teamName(emp.getTeam() != null ? emp.getTeam().getName() : null)
                .groupName(emp.getTeam() != null && emp.getTeam().getGroup() != null
                        ? emp.getTeam().getGroup().getName() : null)
                .tierOrder(snapshot != null ? snapshot.getTierOrder() : null)
                .tierName(snapshot != null ? snapshot.getTierName() : null)
                .sortRank(snapshot != null ? snapshot.getSortRank() : null)
                .priorityReason(buildPriorityReason(snapshot))
                .lastTrainedDate(lastTraining != null ? lastTraining.getActualDate() : null)
                .lastTrainedPassed(lastTraining != null ? lastTraining.getIsPass() : null)
                .inCurrentPlan(inPlanIds.contains(emp.getId()))
                .build();
    }

    private String buildPriorityReason(PrioritySnapshotDetail snapshot) {
        if (snapshot == null) return null;
        if ("UNTIERED".equals(snapshot.getTierName())) return "Không có tiêu chí ưu tiên";
        return snapshot.getTierName() + " — Hạng #" + snapshot.getSortRank();
    }
}