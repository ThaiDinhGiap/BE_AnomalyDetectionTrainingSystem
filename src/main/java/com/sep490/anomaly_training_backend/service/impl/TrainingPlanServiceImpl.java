// src/main/java/com/sep490/anomaly_training_backend/service/impl/TrainingPlanServiceImpl.java
package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.ScheduleRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.exception.ResourceNotFoundException;
import com.sep490.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.util.ReportUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanDetailRepository trainingPlanDetailRepository;
    private final GroupRepository groupRepository;
    private final TrainingPlanMapper mapper;
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

    @Override
    @Transactional
    public TrainingPlanResponse createPlan(TrainingPlanCreateRequest request) {
        if (request.getMonthEnd().isBefore(request.getMonthStart())) {
            throw new IllegalArgumentException("Tháng kết thúc không được nhỏ hơn tháng bắt đầu");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Team> managedTeams = teamRepository.findAllByTeamLeaderId(currentUser.getId());

        if (managedTeams.isEmpty()) {
            throw new IllegalStateException("Bạn không phải là Team Lead của bất kỳ nhóm nào.");
        }

        List<Team> validTeams = managedTeams.stream()
                .filter(t -> t.getGroup() != null && t.getGroup().getId().equals(request.getGroupId()))
                .toList();

        if (validTeams.isEmpty()) {
            throw new IllegalStateException("Bạn không có quyền tạo kế hoạch cho Dây chuyền này (ID: " + request.getGroupId() + ")");
        }
        Group selectedGroup = validTeams.get(0).getGroup();

        // Validate và lấy ProductLine
        ProductLine productLine = productLineRepository.findById(request.getLineId())
                .orElseThrow(() -> new EntityNotFoundException("Product Line ID " + request.getLineId() + " không tồn tại"));

        // Validate ProductLine thuộc Group đã chọn
        if (!productLine.getGroup().getId().equals(selectedGroup.getId())) {
            throw new IllegalArgumentException("Product Line không thuộc Dây chuyền đã chọn");
        }

        TrainingPlan trainingPlan = mapper.toEntity(request);
        trainingPlan.setCreatedBy(currentUser.getUsername());
        trainingPlan.setStatus(ReportStatus.DRAFT);
        trainingPlan.setCurrentVersion(1);
        trainingPlan.setLine(productLine);
        trainingPlan.setTeam(validTeams.get(0));

        TrainingPlan savedPlan = trainingPlanRepository.save(trainingPlan);

        TrainingPlanResponse response = mapper.toResponse(savedPlan);

        List<Long> validTeamIds = validTeams.stream().map(Team::getId).toList();

        List<Employee> teamMembers = employeeRepository.findAllByTeamIdIn(validTeamIds);

        // B3: Map Employee sang TrainingPlanDetailResponse
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
        populateEmployeeProcesses(response, savedPlan);

        return response;
    }

    @Override
    public List<GroupResponse> getMyManagedGroups() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();

        List<Team> managedTeams = teamRepository.findAllByTeamLeaderId(currentUser.getId());

        return managedTeams.stream()
                .map(Team::getGroup)
                .distinct()
                .map(group -> new GroupResponse(group.getId(), group.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public TrainingPlanResponse getPlanDetail(Long id) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch với ID: " + id));

        TrainingPlanResponse response = mapper.toResponse(plan);
        populateEmployeeProcesses(response, plan);
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
                                    .findByEmployeeIdAndProcessProductLineId(empId, productLineId);
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



    @Override
    public List<TrainingPlanResponse> getAllPlans() {
        List<TrainingPlan> plans = trainingPlanRepository.findAll();

        return plans.stream()
                .map(plan -> {
                    TrainingPlanResponse response = mapper.toResponse(plan);
                    populateEmployeeProcesses(response, plan);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainingPlanResponse> getRejectedPlans() {
        List<ReportStatus> rejectedStatuses = List.of(
                ReportStatus.REVISE,
                ReportStatus.REJECTED_BY_SV,
                ReportStatus.REJECTED_BY_MANAGER
        );
        List<TrainingPlan> plans = trainingPlanRepository.findByStatusInAndDeleteFlagFalse(rejectedStatuses);
        return plans.stream()
                .map(plan -> {
                    TrainingPlanResponse response = mapper.toResponse(plan);
                    populateEmployeeProcesses(response, plan);
                    return response;
                })
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
        // 1. Load plan (aggregate root)
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", planId));

        // 2. Validate Employee
        Employee employee = getValidatedEmployee(request.getEmployeeId());

        // 3. Tạo detail rows cho từng schedule — add through aggregate root
        List<TrainingPlanDetail> addedDetails = new ArrayList<>();
        String batchId = java.util.UUID.randomUUID().toString();
        if (request.getSchedules() != null) {
            for (ScheduleRequest schedule : request.getSchedules()) {
                if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                    TrainingPlanDetail detail = createBaseDetail(plan, employee, request.getNote(), schedule);
                    detail.setStatus(TrainingPlanDetailStatus.PENDING);
                    detail.setBatchId(batchId);
                    plan.addDetail(detail); // Aggregate root method — validates editable + sets back-reference
                    addedDetails.add(detail);
                }
            }
        }

        if (addedDetails.isEmpty()) {
            throw new BusinessException("Cần có ít nhất 1 ngày huấn luyện được lên kế hoạch.");
        }

        trainingPlanRepository.save(plan);

        // Trả về detail đầu tiên kèm employeeProcesses
        TrainingPlanDetail firstAdded = addedDetails.get(0);
        TrainingPlanDetailResponse response = mapper.toDetailResponse(firstAdded);
        populateDetailProcesses(response, plan);
        return response;
    }

    @Override
    @Transactional
    public TrainingPlanDetailResponse updateDetail(Long planId, Long detailId, TrainingPlanDetailRequest request) {
        // 1. Load plan (aggregate root)
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", planId));

        // 2. Validate editable status through aggregate root
        if (!plan.isEditable()) {
            throw new BusinessException("Không thể chỉnh sửa chi tiết khi kế hoạch đang chờ duyệt.");
        }

        // 3. Tìm detail within aggregate
        TrainingPlanDetail detail = plan.findDetailById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlanDetail", "id", detailId));

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
                throw new BusinessException("Ngày " + schedule.getPlannedDay() +
                        " không hợp lệ trong tháng " + targetMonth.getMonthValue());
            }
        }

        trainingPlanRepository.save(plan);
        TrainingPlanDetailResponse response = mapper.toDetailResponse(detail);
        populateDetailProcesses(response, plan);
        return response;
    }

    @Override
    public List<EmployeeResponse> getEmployeesNotInPlan(Long planId) {
        // 1. Load plan để lấy thông tin team/group
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", planId));

        // 2. Lấy tất cả employee thuộc cùng group/team
        Long groupId = plan.getLine() != null && plan.getLine().getGroup() != null
                ? plan.getLine().getGroup().getId()
                : (plan.getTeam() != null && plan.getTeam().getGroup() != null
                ? plan.getTeam().getGroup().getId() : null);

        if (groupId == null) {
            return List.of();
        }

        List<Employee> allEmployees = employeeRepository.findAllActiveByGroupId(groupId);

        // 3. Lấy employee IDs đã có trong plan
        List<Long> existingEmployeeIds = trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(planId);

        // 4. Lọc: chỉ lấy những người chưa có trong plan
        return allEmployees.stream()
                .filter(emp -> !existingEmployeeIds.contains(emp.getId()))
                .map(emp -> {
                    EmployeeResponse res = new EmployeeResponse();
                    res.setId(emp.getId());
                    res.setEmployeeCode(emp.getEmployeeCode());
                    res.setFullName(emp.getFullName());
                    res.setStatus(emp.getStatus());
                    res.setTeamId(emp.getTeam() != null ? emp.getTeam().getId() : null);
                    res.setTeamName(emp.getTeam() != null ? emp.getTeam().getName() : null);
                    res.setGroupName(emp.getTeam() != null && emp.getTeam().getGroup() != null
                            ? emp.getTeam().getGroup().getName() : null);
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesInTeams(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", planId));

        // Lấy teamId từ plan
        Long teamId = plan.getTeam() != null ? plan.getTeam().getId() : null;

        if (teamId == null) {
            return List.of();
        }

        List<Employee> allEmployees = employeeRepository.findAllActiveByTeamId(teamId);

        return allEmployees.stream()
                .map(emp -> {
                    EmployeeResponse res = new EmployeeResponse();
                    res.setId(emp.getId());
                    res.setEmployeeCode(emp.getEmployeeCode());
                    res.setFullName(emp.getFullName());
                    res.setStatus(emp.getStatus());
                    res.setTeamId(emp.getTeam() != null ? emp.getTeam().getId() : null);
                    res.setTeamName(emp.getTeam() != null ? emp.getTeam().getName() : null);
                    res.setGroupName(emp.getTeam() != null && emp.getTeam().getGroup() != null
                            ? emp.getTeam().getGroup().getName() : null);
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrainingPlanResponse updatePlan(Long planId, TrainingPlanUpdateRequest request) {
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch ID: " + planId));

        // Validate editable through aggregate root
        if (!plan.isEditable()) {
            throw new IllegalStateException("Không thể chỉnh sửa khi đang chờ duyệt.");
        }

        // 1. UPDATE HEADER through aggregate root
        updateHeaderIfPresent(plan, request);

        // 2. UPDATE DETAILS (chỉ khi list != null)
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            processDetailActions(plan, request.getDetails(), plan.isApproved());
        }

        // 3. Lưu plan
        TrainingPlan savedPlan = trainingPlanRepository.save(plan);

        TrainingPlanResponse response = mapper.toResponse(savedPlan);
        populateEmployeeProcesses(response, savedPlan);
        return response;
    }

    // ==================== HEADER UPDATE ====================

    private void updateHeaderIfPresent(TrainingPlan plan, TrainingPlanUpdateRequest request) {
        // Delegate header update to aggregate root
        plan.updateHeader(request.getTitle(), request.getNote(), request.getMonthStart(), request.getMonthEnd());

        if (request.getLineId() != null) {
            ProductLine line = productLineRepository.findById(request.getLineId())
                    .orElseThrow(() -> new EntityNotFoundException("Product Line ID " + request.getLineId() + " không tồn tại"));
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
                throw new BusinessException("Thiếu trường 'action' trong detail request");
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
     * ADD: Thêm employee vào plan, process tự lấy từ employee_skill.
     * Uses aggregate root addDetail() for invariant enforcement.
     */
    private void handleAddAction(TrainingPlan plan,
                                  TrainingPlanUpdateRequest.DetailAction action,
                                  Long productLineId) {
        if (action.getEmployeeId() == null) {
            throw new BusinessException("employeeId là bắt buộc khi action = ADD");
        }

        Employee employee = getValidatedEmployee(action.getEmployeeId());

        if (action.getSchedules() == null || action.getSchedules().isEmpty()) {
            throw new BusinessException("Cần có ít nhất 1 ngày huấn luyện khi thêm detail mới.");
        }

        // Tạo batchId chung cho lần ADD này → phân biệt các lần thêm khác nhau của cùng 1 employee
        String batchId = java.util.UUID.randomUUID().toString();

        for (ScheduleRequest schedule : action.getSchedules()) {
            if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                TrainingPlanDetail detail = createBaseDetail(plan, employee, action.getNote(), schedule);
                detail.setStatus(TrainingPlanDetailStatus.PENDING);
                detail.setBatchId(batchId);
                plan.addDetail(detail); // Aggregate root method
            }
        }
    }

    /**
     * ADD_SCHEDULE: Thêm ngày vào batch cũ (giữ nguyên row trên FE).
     * Uses aggregate root findDetailsByBatchId() and addDetail().
     */
    private void handleAddScheduleAction(TrainingPlan plan,
                                          TrainingPlanUpdateRequest.DetailAction action) {
        if (action.getBatchId() == null || action.getBatchId().isBlank()) {
            throw new BusinessException("batchId là bắt buộc khi action = ADD_SCHEDULE");
        }

        if (action.getSchedules() == null || action.getSchedules().isEmpty()) {
            throw new BusinessException("Cần có ít nhất 1 ngày huấn luyện khi action = ADD_SCHEDULE");
        }

        // Tìm employee từ batch hiện tại through aggregate root
        Employee employee;
        if (action.getEmployeeId() != null) {
            employee = getValidatedEmployee(action.getEmployeeId());
        } else {
            List<TrainingPlanDetail> batchDetails = plan.findDetailsByBatchId(action.getBatchId());
            if (batchDetails.isEmpty()) {
                throw new BusinessException("Không tìm thấy batch với ID: " + action.getBatchId());
            }
            employee = batchDetails.get(0).getEmployee();
        }

        // Note: nếu không gửi note → lấy note từ detail cũ cùng batch
        String note = action.getNote();
        if (note == null) {
            note = plan.findDetailsByBatchId(action.getBatchId()).stream()
                    .map(TrainingPlanDetail::getNote)
                    .findFirst()
                    .orElse(null);
        }

        for (ScheduleRequest schedule : action.getSchedules()) {
            if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {
                TrainingPlanDetail detail = createBaseDetail(plan, employee, note, schedule);
                detail.setStatus(TrainingPlanDetailStatus.PENDING);
                detail.setBatchId(action.getBatchId()); // Giữ nguyên batchId cũ
                plan.addDetail(detail); // Aggregate root method
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
            throw new BusinessException("detailId là bắt buộc khi action = UPDATE");
        }

        TrainingPlanDetail detail = detailMap.get(action.getDetailId());
        if (detail == null) {
            throw new ResourceNotFoundException("TrainingPlanDetail", "id", action.getDetailId());
        }

        // Bản APPROVED: không cho sửa detail đã hoàn thành
        if (isApproved && detail.getStatus() == TrainingPlanDetailStatus.DONE) {
            throw new BusinessException("Không thể sửa detail đã hoàn thành (ID: " + action.getDetailId() + ")");
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
                throw new BusinessException("Ngày " + action.getPlannedDay()
                        + " không hợp lệ trong tháng " + targetMonth.getMonthValue());
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
                throw new BusinessException("Ngày " + schedule.getPlannedDay()
                        + " không hợp lệ trong tháng " + targetMonth.getMonthValue());
            }
        }
    }

    /**
     * DELETE: Uses aggregate root removeOrMarkMissed() for status-based behavior.
     */
    private void handleDeleteAction(TrainingPlan plan,
                                     TrainingPlanUpdateRequest.DetailAction action,
                                     Map<Long, TrainingPlanDetail> detailMap,
                                     boolean isApproved) {
        if (action.getDetailId() == null) {
            throw new BusinessException("detailId là bắt buộc khi action = DELETE");
        }

        TrainingPlanDetail detail = detailMap.get(action.getDetailId());
        if (detail == null) {
            throw new ResourceNotFoundException("TrainingPlanDetail", "id", action.getDetailId());
        }

        if (!isApproved) {
            // DRAFT/REJECTED: xóa training_result_details con trước rồi xóa qua aggregate root
            trainingResultDetailRepository.deleteByTrainingPlanDetailId(detail.getId());
        }

        // Delegate to aggregate root — handles APPROVED (mark MISSED) vs DRAFT (physical remove)
        plan.removeOrMarkMissed(detail);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", planId));

        // Validate through aggregate root
        if (!plan.isDeletable()) {
            throw new IllegalStateException(
                    "Chỉ có thể xóa kế hoạch ở trạng thái DRAFT hoặc REJECTED. Trạng thái hiện tại: " + plan.getStatus()
            );
        }

        // Verify ownership - user must be the creator or have delete permission
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!plan.getCreatedBy().equals(currentUsername)) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Check if user is team leader of the same group
            boolean isTeamLeader = teamRepository.findAllByTeamLeaderId(currentUser.getId())
                    .stream()
                    .anyMatch(team -> team.getId().equals(plan.getTeam().getId()));

            if (!isTeamLeader) {
                throw new IllegalStateException("Bạn không có quyền xóa kế hoạch này");
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
        // 1. Tìm plan (aggregate root)
        TrainingPlan plan = trainingPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", planId));

        // 2. Validate through aggregate root
        if (!plan.isDeletable()) {
            throw new IllegalStateException(
                    "Chỉ có thể xóa chi tiết khi kế hoạch ở trạng thái DRAFT hoặc REJECTED. Trạng thái hiện tại: " + plan.getStatus()
            );
        }

        // 3. Tìm detail within aggregate
        TrainingPlanDetail detailToRemove = plan.findDetailById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlanDetail", "id", detailId));

        if (detailToRemove.getStatus() != TrainingPlanDetailStatus.PENDING) {
            throw new IllegalStateException(
                    "Chỉ có thể xóa chi tiết khi kế hoạch ở trạng thái PENDING. Trạng thái hiện tại: " + detailToRemove.getStatus()
            );
        }

        // 4. Xóa training_result_details con trước (FK constraint)
        trainingResultDetailRepository.deleteByTrainingPlanDetailId(detailToRemove.getId());

        // 5. Remove through aggregate root (orphanRemoval=true sẽ xóa khỏi DB)
        plan.removeDetail(detailToRemove);

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
                .findByEmployeeIdAndProcessProductLineId(detailResponse.getEmployeeId(), productLineId);

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
                .orElseThrow(() -> new EntityNotFoundException("Nhân viên ID " + employeeId + " không tồn tại"));
    }

    private Process getValidatedProcess(Long processId, Long productLineId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new EntityNotFoundException("Công đoạn ID " + processId + " không tồn tại"));

        // Validate Process thuộc ProductLine được chọn trong Plan
        if (productLineId != null && process.getProductLine() != null) {
            if (!process.getProductLine().getId().equals(productLineId)) {
                throw new IllegalArgumentException(
                        "Công đoạn '" + process.getName() + "' không thuộc ProductLine được chọn trong kế hoạch này"
                );
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
            throw new IllegalArgumentException("Ngày " + schedule.getPlannedDay() +
                    " không hợp lệ trong tháng " + targetMonth.getMonthValue());
        }

        return detailEntity;
    }

    // Relate approval methods
    @Override
    public void submitPlanForApproval(Long planId, User currentUser, HttpServletRequest request) {
        TrainingPlan plan = getReportById(planId);

        // Delegate validation to aggregate root
        plan.validateForSubmission();

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
            throw new BusinessException("Only author can edit on this proposal");
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

    private TrainingPlan getReportById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingPlan", "id", id));
    }

    // Validation is now handled by TrainingPlan.validateForSubmission() (aggregate root)

    /**
     * Hàm private thực hiện việc copy dữ liệu từ Plan -> History Entity
     */
    private void createHistorySnapshot(TrainingPlan plan) {
        TrainingPlanHistory history = TrainingPlanHistory.builder()
                .trainingPlan(plan)
                .version(plan.getCurrentVersion() == null ? 1 : plan.getCurrentVersion())
                .title(plan.getTitle())
                .formCode(plan.getFormCode())
                .monthStart(plan.getMonthStart())
                .monthEnd(plan.getMonthEnd())
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
}