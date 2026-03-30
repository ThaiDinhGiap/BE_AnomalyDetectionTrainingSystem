package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
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
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.enums.PolicyEntityType;
import com.sep490.anomaly_training_backend.enums.PolicyStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
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
import com.sep490.anomaly_training_backend.repository.TrainingPlanSpecialDayRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.priority.TrainingPlanScheduleGenerationService;
import com.sep490.anomaly_training_backend.service.priority.impl.PriorityScoringServiceImpl;
import com.sep490.anomaly_training_backend.util.ReportUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private static final String PERM_VIEW_SECTION = "training_plan.view.section";
    private static final String PERM_VIEW_OWN_GROUP = "training_plan.view.own_group";
    private static final String PERM_VIEW_CROSS_GROUP = "training_plan.view.cross_group";

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
    private final TrainingPlanSpecialDayRepository trainingPlanSpecialDayRepository;

    @Override
    public List<GroupResponse> getMyManagedGroups() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

    // ── Query: getAllPlans (refactored) ───────────────────────────────────────

    /**
     * Trả về danh sách training plans mà user được phép xem, dựa trên scope permission.
     * <p>
     * Thứ tự ưu tiên (từ rộng đến hẹp):
     * 1. training_plan.view.section     → Manager: thấy toàn section, loại trừ DRAFT/REVISE
     * 2. training_plan.view.own_group   → Supervisor: thấy các group mình phụ trách
     * 3. training_plan.view.cross_group → Final Inspection: thấy groups liên kết với FI
     * 4. (default)                      → Team Leader: chỉ thấy plans do mình tạo
     * <p>
     * Không hardcode role name → admin có thể gán scope permission cho bất kỳ role nào.
     */
    @Override
    public List<TrainingPlanGenerationResponse> getAllPlans(User currentUser, Long lineId) {

        if (currentUser.hasPermission(PERM_VIEW_SECTION)) {
            return fetchBySection(currentUser, lineId);
        }

        if (currentUser.hasPermission(PERM_VIEW_OWN_GROUP)) {
            return fetchByOwnGroups(currentUser, lineId);
        }

        if (currentUser.hasPermission(PERM_VIEW_CROSS_GROUP)) {
            return fetchByCrossGroup(currentUser, lineId);
        }

        // Default: Team Leader — chỉ thấy plans do chính mình tạo
        return fetchByCreator(currentUser.getUsername(), lineId);
    }

    /**
     * Trả về danh sách plans bị từ chối, áp dụng cùng scope logic với getAllPlans.
     * <p>
     * Trước đây không nhận currentUser → trả về ALL rejected plans cho mọi user.
     * Controller cần cập nhật signature để truyền currentUser vào.
     */
    @Override
    public List<TrainingPlanGenerationResponse> getRejectedPlans(User currentUser) {
        List<ReportStatus> rejectedStatuses = List.of(
                ReportStatus.REVISING,
                ReportStatus.REJECTED);

        // Reuse scope logic, sau đó filter thêm theo status
        return getAllPlans(currentUser, null)
                .stream()
                .filter(r -> rejectedStatuses.contains(r.getTrainingPlan().getStatus()))
                .collect(Collectors.toList());
    }

    // ── Private: scope query helpers ─────────────────────────────────────────

    /**
     * Section scope (Manager):
     * - Tìm plans thuộc section mà user là manager
     * - Loại trừ DRAFT và REVISE (manager không cần thấy bản nháp của TL)
     */
    private List<TrainingPlanGenerationResponse> fetchBySection(User currentUser, Long lineId) {
        List<ReportStatus> excludedStatuses = List.of(ReportStatus.DRAFT, ReportStatus.REVISING);
        List<TrainingPlan> plans = lineId != null
                ? trainingPlanRepository.findAllByManagerAndLineIdAndDeleteFlagFalse(
                currentUser.getId(), lineId, excludedStatuses)
                : trainingPlanRepository.findAllByManagerAndDeleteFlagFalse(
                currentUser.getId(), excludedStatuses);
        return toResponses(plans);
    }

    /**
     * Own group scope (Supervisor):
     * - Tìm tất cả groups mà user là supervisor
     * - Trả về tất cả plans trong các groups đó (kể cả DRAFT của TL dưới quyền)
     */
    private List<TrainingPlanGenerationResponse> fetchByOwnGroups(User currentUser, Long lineId) {
        List<Group> groups = groupRepository.findBySupervisorId(currentUser.getId());
        if (groups.isEmpty()) return Collections.emptyList();

        List<Long> groupIds = groups.stream()
                .map(Group::getId)
                .distinct()
                .toList();

        return fetchByGroupIds(groupIds, lineId);
    }

    /**
     * Cross group scope (Final Inspection):
     * - FI không thuộc hierarchy section→group→team bình thường
     * - Tìm các teams mà FI user được liên kết, lấy groupIds từ đó
     */
    private List<TrainingPlanGenerationResponse> fetchByCrossGroup(User currentUser, Long lineId) {
        List<Team> teams = teamRepository.findByFinalInspectionId(currentUser.getId());
        if (teams.isEmpty()) return Collections.emptyList();

        List<Long> groupIds = teams.stream()
                .map(team -> team.getGroup().getId())
                .distinct()
                .toList();

        return fetchByGroupIds(groupIds, lineId);
    }

    /**
     * Creator scope (Team Leader — default):
     * - Chỉ trả về plans do chính user này tạo
     */
    private List<TrainingPlanGenerationResponse> fetchByCreator(String username, Long lineId) {
        List<TrainingPlan> plans = lineId != null
                ? trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse(username, lineId)
                : trainingPlanRepository.findByCreatedByAndDeleteFlagFalse(username);
        return toResponses(plans);
    }

    /**
     * Shared helper: query theo danh sách groupIds + optional lineId filter.
     */
    private List<TrainingPlanGenerationResponse> fetchByGroupIds(List<Long> groupIds, Long lineId) {
        List<TrainingPlan> plans = lineId != null
                ? trainingPlanRepository.findAllByGroupIdsAndLineIdAndDeleteFlagFalse(groupIds, lineId)
                : trainingPlanRepository.findAllByGroupIdsAndDeleteFlagFalse(groupIds);
        return toResponses(plans);
    }

    /**
     * Shared helper: convert list TrainingPlan → list response.
     */
    private List<TrainingPlanGenerationResponse> toResponses(List<TrainingPlan> plans) {
        return plans.stream()
                .map(this::toGenerationResponse)
                .collect(Collectors.toList());
    }

    // ── Mutation ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TrainingPlanGenerationResponse updatePlan(Long planId, TrainingPlanUpdateRequest request) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (plan.getStatus() == ReportStatus.PENDING_REVIEW || plan.getStatus() == ReportStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        boolean isApproved = ReportStatus.COMPLETED.equals(plan.getStatus());

        updateHeaderIfPresent(plan, request);

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            processDetailActions(plan, request.getDetails(), isApproved);
        }

        TrainingPlan savedPlan = trainingPlanRepository.save(plan);

        if (isApproved) {
            regenerateResultDetails(savedPlan);
        }

        return toGenerationResponse(savedPlan);
    }

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

    private void processDetailActions(TrainingPlan plan,
                                      List<TrainingPlanUpdateRequest.DetailAction> actions,
                                      boolean isApproved) {
        Long productLineId = (plan.getLine() != null) ? plan.getLine().getId() : null;

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

        String batchId = java.util.UUID.randomUUID().toString();

        for (ScheduleRequest schedule : action.getSchedules()) {
            if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                TrainingPlanDetail detail = createBaseDetail(plan, employee, action.getNote(), schedule);
                detail.setStatus(ReportStatus.PENDING_REVIEW);
                detail.setBatchId(batchId);
                plan.getDetails().add(detail);
            }
        }
    }

    private void handleAddScheduleAction(TrainingPlan plan,
                                         TrainingPlanUpdateRequest.DetailAction action) {
        if (action.getBatchId() == null || action.getBatchId().isBlank()) {
            throw new AppException(ErrorCode.MISSING_BATCH_ID);
        }

        if (action.getSchedules() == null || action.getSchedules().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_SCHEDULE);
        }

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

        String note = action.getNote();
        if (note == null) {
            note = plan.getDetails().stream()
                    .filter(d -> action.getBatchId().equals(d.getBatchId()))
                    .findFirst()
                    .map(TrainingPlanDetail::getNote)
                    .orElse(null);
        }

        for (ScheduleRequest schedule : action.getSchedules()) {
            if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                TrainingPlanDetail detail = createBaseDetail(plan, employee, note, schedule);
                detail.setStatus(ReportStatus.PENDING_REVIEW);
                detail.setBatchId(action.getBatchId());
                plan.getDetails().add(detail);
            }
        }
    }

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

        if (isApproved && detail.getStatus() == ReportStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_COMPLETED_DETAIL);
        }

        if (action.getEmployeeId() != null) {
            detail.setEmployee(getValidatedEmployee(action.getEmployeeId()));
        }
        if (action.getNote() != null) {
            detail.setNote(action.getNote());
        }

        if (action.getTargetMonth() != null && action.getPlannedDay() != null) {
            LocalDate targetMonth = action.getTargetMonth().withDayOfMonth(1);
            detail.setTargetMonth(targetMonth);
            try {
                detail.setPlannedDate(targetMonth.withDayOfMonth(action.getPlannedDay()));
            } catch (DateTimeException e) {
                throw new AppException(ErrorCode.INVALID_DAY_OF_MONTH);
            }
        }

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
            if (detail.getStatus() == ReportStatus.COMPLETED) {
                throw new AppException(ErrorCode.CANNOT_DELETE_COMPLETED_DETAIL);
            }
            detail.setStatus(ReportStatus.MISSED);
            detail.setNote("[Đã hủy] " + (detail.getNote() != null ? detail.getNote() : ""));
        } else {
            trainingResultDetailRepository.deleteByTrainingPlanDetailId(detail.getId());
            plan.getDetails().remove(detail);
        }
    }

    @Override
    @Transactional
    public TrainingPlanDetailResponse addDetail(Long planId, TrainingPlanDetailRequest request) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (plan.getStatus() == ReportStatus.PENDING_REVIEW || plan.getStatus() == ReportStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        Employee employee = getValidatedEmployee(request.getEmployeeId());

        List<TrainingPlanDetail> addedDetails = new ArrayList<>();
        String batchId = java.util.UUID.randomUUID().toString();
        if (request.getSchedules() != null) {
            for (ScheduleRequest schedule : request.getSchedules()) {
                if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                    TrainingPlanDetail detail = createBaseDetail(plan, employee, request.getNote(), schedule);
                    detail.setStatus(ReportStatus.PENDING_REVIEW);
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

        if (ReportStatus.COMPLETED.equals(plan.getStatus())) {
            regenerateResultDetails(plan);
        }

        TrainingPlanDetail firstAdded = addedDetails.get(0);
        TrainingPlanDetailResponse response = planMapper.toDetailResponse(firstAdded);
        populateDetailProcesses(response, plan);
        return response;
    }

    @Override
    @Transactional
    public TrainingPlanDetailResponse updateDetail(Long planId, Long detailId, TrainingPlanDetailRequest request) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (plan.getStatus() == ReportStatus.PENDING_REVIEW || plan.getStatus() == ReportStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        TrainingPlanDetail detail = plan.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND));

        if (request.getEmployeeId() != null) {
            Employee employee = getValidatedEmployee(request.getEmployeeId());
            detail.setEmployee(employee);
        }

        if (request.getNote() != null) {
            detail.setNote(request.getNote());
        }

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
    @Transactional
    public void deletePlan(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (plan.getStatus() != ReportStatus.DRAFT && plan.getStatus() != ReportStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!plan.getCreatedBy().equals(currentUsername)) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            boolean isTeamLeader = teamRepository.findAllByTeamLeaderId(currentUser.getId())
                    .stream()
                    .anyMatch(team -> team.getId().equals(plan.getTeam().getId()));

            if (!isTeamLeader) {
                throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
            }
        }

        List<Long> detailIds = plan.getDetails().stream()
                .map(TrainingPlanDetail::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!detailIds.isEmpty()) {
            trainingResultDetailRepository.deleteByTrainingPlanDetailIdIn(detailIds);
        }

        List<Long> specialDayIds = plan.getSpecialDays().stream()
                .map(TrainingPlanSpecialDay::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!specialDayIds.isEmpty()) {
            trainingPlanSpecialDayRepository.deleteByIdIn(specialDayIds);
        }

        PrioritySnapshot prioritySnapshot = prioritySnapshotRepository.findByTrainingPlanId(planId).orElse(null);
        if (prioritySnapshot != null) {
            List<Long> prioritySnapshotDetailIds = prioritySnapshot.getDetails().stream()
                    .map(PrioritySnapshotDetail::getId)
                    .filter(Objects::nonNull)
                    .toList();
            if (!prioritySnapshotDetailIds.isEmpty()) {
                prioritySnapshotDetailRepository.deleteByIdIn(prioritySnapshotDetailIds);
            }
            prioritySnapshotRepository.delete(prioritySnapshot);
        }

        trainingPlanRepository.delete(plan);
    }

    @Override
    @Transactional
    public void deleteDetail(Long planId, Long detailId) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (plan.getStatus() != ReportStatus.DRAFT
                && plan.getStatus() != ReportStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        TrainingPlanDetail detailToRemove = plan.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND));

        if (detailToRemove.getStatus() != ReportStatus.PENDING_REVIEW) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        trainingResultDetailRepository.deleteByTrainingPlanDetailId(detailToRemove.getId());
        plan.getDetails().remove(detailToRemove);
        trainingPlanRepository.save(plan);
    }

    // ── Lookup ───────────────────────────────────────────────────────────────

    @Override
    public List<PrioritizedEmployeeResponse> getEmployeesNotInPlan(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        Long groupId = plan.getLine() != null && plan.getLine().getGroup() != null
                ? plan.getLine().getGroup().getId()
                : (plan.getTeam() != null && plan.getTeam().getGroup() != null
                ? plan.getTeam().getGroup().getId()
                : null);

        if (groupId == null) return List.of();

        List<Employee> allEmployees = employeeRepository.findAllActiveByGroupId(groupId, EmployeeStatus.ACTIVE);
        Set<Long> inPlanIds = new java.util.HashSet<>(
                trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(planId));

        Map<Long, PrioritySnapshotDetail> snapshotMap = loadSnapshotMap(planId);
        Map<Long, TrainingResultDetail> lastTrainingMap = loadLastTrainingMap(
                allEmployees.stream().map(Employee::getId).collect(Collectors.toList()));

        return allEmployees.stream()
                .filter(emp -> !inPlanIds.contains(emp.getId()))
                .map(emp -> buildPrioritizedEmployeeResponse(emp, snapshotMap, lastTrainingMap, inPlanIds))
                .collect(Collectors.toList());
    }

    @Override
    public List<PrioritizedEmployeeResponse> getEmployeesInTeams(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        Long teamId = plan.getTeam() != null ? plan.getTeam().getId() : null;
        if (teamId == null) return List.of();

        List<Employee> allEmployees = employeeRepository.findAllActiveByTeamId(teamId, EmployeeStatus.ACTIVE);
        Set<Long> inPlanIds = new java.util.HashSet<>(
                trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(planId));

        Map<Long, PrioritySnapshotDetail> snapshotMap = loadSnapshotMap(planId);
        Map<Long, TrainingResultDetail> lastTrainingMap = loadLastTrainingMap(
                allEmployees.stream().map(Employee::getId).collect(Collectors.toList()));

        return allEmployees.stream()
                .map(emp -> buildPrioritizedEmployeeResponse(emp, snapshotMap, lastTrainingMap, inPlanIds))
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
                .build()).collect(Collectors.toList());
    }

    // ── Generate ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TrainingPlanGenerationResponse generateTrainingPlans(User currentUser,
                                                                TrainingPlanGenerationRequest request) {
        TrainingPlan generatedTrainingPlan = generateTrainingPlan(request);

        List<Employee> teamMembers = employeeRepository.findAllActiveByTeamId(request.getTeamId(), EmployeeStatus.ACTIVE);
        PriorityPolicy priorityPolicy = policyRepository
                .findFirstByEntityTypeAndStatusAndDeleteFlagFalse(PolicyEntityType.EMPLOYEE, PolicyStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.POLICY_NOT_FOUND,
                        "No active priority policy found for employees"));

        PrioritySnapshot prioritySnapshot = priorityScoringService.generateSnapshot(
                priorityPolicy.getId(), request.getTeamId(), teamMembers);
        prioritySnapshot.setTrainingPlan(generatedTrainingPlan);
        prioritySnapshotRepository.save(prioritySnapshot);

        // Use plan's start year for calendar lookup (not current year)
        int calendarYear = request.getStartDate().getYear();

        TrainingPlan scheduledPlan = trainingPlanScheduleGenerationService.generateOptimalSchedule(
                generatedTrainingPlan.getId(),
                prioritySnapshot.getId(),
                calendarYear);

        // Reload snapshot with details (lazy-loaded) for proper response mapping
        PrioritySnapshot reloadedSnapshot = prioritySnapshotRepository.findById(prioritySnapshot.getId())
                .orElse(prioritySnapshot);
        PrioritySnapshotResponse prioritySnapshotResponse = prioritySnapshotMapper.toResponse(reloadedSnapshot);

        TrainingPlanGenerationResponse response = new TrainingPlanGenerationResponse();
        response.setPrioritySnapshot(prioritySnapshotResponse);
        response.setTrainingPlan(toTrainingPlanResponse(scheduledPlan));

        return response;
    }

    // ── Approval workflow ────────────────────────────────────────────────────

    @Override
    public void submitPlanForApproval(Long reportId, User currentUser, HttpServletRequest request) {
        TrainingPlan report = getReportById(reportId);
        validatePlanForSubmission(report);
        report.setFormCode(
                ReportUtils.generateFormCode(ApprovalEntityType.TRAINING_PLAN, report.getLine().getCode(), reportId));
        approvalService.submit(report, currentUser, request);
        trainingPlanRepository.save(report);
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
    public ResponseEntity<Boolean> canApprove(Long reportId, User currentUser) {
        try {
            TrainingPlan report = getReportById(reportId);
            Boolean hasPermission = approvalService.canApprove(report, currentUser);
            return ResponseEntity.ok(hasPermission);
        } catch (AppException e) {
            return ResponseEntity.ok(Boolean.FALSE);
        }
    }

    // ── Feedback ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void clearFeedback(Long proposalId) {
        List<TrainingPlanDetail> details = trainingPlanDetailRepository
                .findByTrainingPlanIdAndDeleteFlagFalse(proposalId);
        details.forEach(d -> d.setRejectFeedback(null));
        trainingPlanDetailRepository.saveAll(details);
        log.info("[RejectFeedback] Đã xoá toàn bộ feedback của proposalId={}", proposalId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private void populateDetailProcesses(TrainingPlanDetailResponse detailResponse, TrainingPlan plan) {
        Long productLineId = plan.getLine() != null ? plan.getLine().getId() : null;
        if (productLineId == null || detailResponse.getEmployeeId() == null) return;

        List<EmployeeSkill> skills = employeeSkillRepository
                .findValidSkillsByEmployeeAndLine(detailResponse.getEmployeeId(), productLineId);

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

    private void regenerateResultDetails(TrainingPlan plan) {
        List<TrainingResult> results = trainingResultRepository.findByTrainingPlanId(plan.getId());
        if (results.isEmpty()) return;

        TrainingResult result = results.get(0);

        Set<Long> existingPlanDetailIds = result.getDetails().stream()
                .filter(rd -> rd.getTrainingPlanDetail() != null)
                .map(rd -> rd.getTrainingPlanDetail().getId())
                .collect(Collectors.toSet());

        for (TrainingPlanDetail planDetail : plan.getDetails()) {
            if (planDetail.getId() != null && !existingPlanDetailIds.contains(planDetail.getId())) {
                TrainingResultDetail newResultDetail = new TrainingResultDetail();
                newResultDetail.setTrainingResult(result);
                newResultDetail.setTrainingPlanDetail(planDetail);
                newResultDetail.setEmployee(planDetail.getEmployee());
                newResultDetail.setPlannedDate(planDetail.getPlannedDate());
                newResultDetail.setBatchId(planDetail.getBatchId());
                newResultDetail.setStatus(com.sep490.anomaly_training_backend.enums.ReportStatus.PENDING_REVIEW);
                result.getDetails().add(newResultDetail);
            }
        }

        trainingResultRepository.save(result);

        String planCreator = plan.getCreatedBy();
        trainingResultDetailRepository.updateCreatedByForResult(result.getId(), planCreator);
    }

    private Employee getValidatedEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private Process getValidatedProcess(Long processId, Long productLineId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        if (productLineId != null && process.getProductLine() != null) {
            if (!process.getProductLine().getId().equals(productLineId)) {
                throw new AppException(ErrorCode.PROCESS_NOT_IN_PRODUCT_LINE);
            }
        }

        return process;
    }

    private TrainingPlanDetail createBaseDetail(TrainingPlan plan, Employee employee, String note,
                                                ScheduleRequest schedule) {
        TrainingPlanDetail detailEntity = new TrainingPlanDetail();
        detailEntity.setTrainingPlan(plan);
        detailEntity.setEmployee(employee);
        detailEntity.setNote(note);

        LocalDate targetMonth = schedule.getTargetMonth().withDayOfMonth(1);
        detailEntity.setTargetMonth(targetMonth);

        try {
            LocalDate plannedDate = targetMonth.withDayOfMonth(schedule.getPlannedDay());
            detailEntity.setPlannedDate(plannedDate);
        } catch (DateTimeException e) {
            throw new AppException(ErrorCode.INVALID_DAY_OF_MONTH);
        }

        return detailEntity;
    }

    private TrainingPlan getReportById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));
    }

    private void validatePlanForSubmission(TrainingPlan plan) {
        if (plan.getDetails() == null || plan.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.PLAN_HAS_NO_DETAILS);
        }
        if (plan.getTitle() == null || plan.getTitle().trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_PLAN_TITLE);
        }
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        for (TrainingPlanDetail detail : plan.getDetails()) {
            if (detail.getEmployee() == null) {
                throw new AppException(ErrorCode.MISSING_EMPLOYEE_IN_DETAIL);
            }
            if (detail.getPlannedDate() == null) {
                throw new AppException(ErrorCode.MISSING_PLANNED_DATE_IN_DETAIL);
            }
            if (detail.getPlannedDate().isBefore(plan.getStartDate()) ||
                    detail.getPlannedDate().isAfter(plan.getEndDate())) {
                throw new AppException(ErrorCode.PLANNED_DATE_OUT_OF_RANGE);
            }
        }

        for (int i = 0; i < plan.getDetails().size(); i++) {
            TrainingPlanDetail detail1 = plan.getDetails().get(i);
            for (int j = i + 1; j < plan.getDetails().size(); j++) {
                TrainingPlanDetail detail2 = plan.getDetails().get(j);

                boolean sameEmployee = detail1.getEmployee().getId().equals(detail2.getEmployee().getId());
                boolean sameDate = detail1.getPlannedDate().equals(detail2.getPlannedDate());
                boolean sameBatch = detail1.getBatchId() != null && detail1.getBatchId().equals(detail2.getBatchId());

                if (sameEmployee && sameDate && sameBatch) {
                    throw new AppException(ErrorCode.DUPLICATE_TRAINING_SCHEDULE);
                }
            }
        }
    }

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

        if (plan.getDetails() != null) {
            for (TrainingPlanDetail detail : plan.getDetails()) {
                TrainingPlanDetailHistory detailHistory = TrainingPlanDetailHistory.builder()
                        .trainingPlanHistory(history)
                        .employeeId(detail.getEmployee().getId())
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
        List<Employee> teamMembers = employeeRepository.findAllActiveByTeamId(trainingPlan.getTeam().getId(), EmployeeStatus.ACTIVE);

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

    private void populateEmployeeProcesses(TrainingPlanResponse response, TrainingPlan plan) {
        Long productLineId = plan.getLine() != null ? plan.getLine().getId() : null;
        if (response.getDetails() == null || productLineId == null) return;

        Map<Long, List<TrainingPlanDetailResponse.ProcessInfo>> skillCache = new java.util.HashMap<>();

        for (TrainingPlanDetailResponse detail : response.getDetails()) {
            if (detail.getEmployeeId() != null) {
                List<TrainingPlanDetailResponse.ProcessInfo> processes = skillCache.computeIfAbsent(
                        detail.getEmployeeId(),
                        empId -> {
                            List<EmployeeSkill> skills = employeeSkillRepository
                                    .findValidSkillsByEmployeeAndLine(empId, productLineId);
                            return skills.stream()
                                    .map(skill -> {
                                        TrainingPlanDetailResponse.ProcessInfo info =
                                                new TrainingPlanDetailResponse.ProcessInfo();
                                        info.setId(skill.getProcess().getId());
                                        info.setName(skill.getProcess().getName());
                                        return info;
                                    })
                                    .toList();
                        });
                detail.setEmployeeProcesses(processes);
            }
        }

        Map<String, EmployeePlanGroup> groupMap = new java.util.LinkedHashMap<>();
        for (TrainingPlanDetailResponse detail : response.getDetails()) {
            if (detail.getEmployeeId() == null) continue;

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
                prioritySnapshotRepository.findByTrainingPlanId(plan.getId()).orElse(null));

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
                                (d1, d2) -> d1.getTierOrder() <= d2.getTierOrder() ? d1 : d2)))
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
                        (d1, d2) -> d1.getActualDate().isAfter(d2.getActualDate()) ? d1 : d2));
    }

    private PrioritizedEmployeeResponse buildPrioritizedEmployeeResponse(
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
                .teamId(emp.getTeams() != null ? emp.getTeams().get(0).getId() : null)
                .teamName(emp.getTeams() != null ? emp.getTeams().get(0).getName() : null)
                .groupName(emp.getTeams() != null && emp.getTeams().get(0).getGroup() != null
                        ? emp.getTeams().get(0).getGroup().getName()
                        : null)
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