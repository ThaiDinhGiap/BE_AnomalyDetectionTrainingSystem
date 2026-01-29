// src/main/java/com/sep490/anomaly_training_backend/service/impl/TrainingPlanServiceImpl.java
package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.ScheduleRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.exception.ResourceNotFoundException;
import com.sep490.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetailHistory;
import com.sep490.anomaly_training_backend.model.TrainingPlanHistory;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanHistoryRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final GroupRepository groupRepository;
    private final TrainingPlanMapper mapper;
    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TrainingPlanHistoryRepository trainingPlanHistoryRepository;
    private final ApprovalService approvalService;
    private final TrainingResultService trainingResultService;

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

        TrainingPlan trainingPlan = mapper.toEntity(request);
        trainingPlan.setGroup(selectedGroup);
        trainingPlan.setCreatedBy(currentUser.getUsername());
        trainingPlan.setStatus(ReportStatus.DRAFT);
        trainingPlan.setCurrentVersion(1);

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


                detailRes.setId(null);
                detailRes.setProcessId(null);
                detailRes.setProcessName(null);
                detailRes.setPlannedDate(null);
                detailRes.setStatus(null);
                detailRes.setNote("");

                prefilledDetails.add(detailRes);
            }
        }

        response.setDetails(prefilledDetails);

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

        return mapper.toResponse(plan);
    }

    @Override
    public List<TrainingPlanResponse> getAllPlans() {
        List<TrainingPlan> plans = trainingPlanRepository.findAll();

        return plans.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessResponse> getProcessesByGroup(Long groupId) {
        List<Process> processes = processRepository.findByGroupId(groupId);

        return processes.stream()
                .map(p -> new ProcessResponse(p.getId(), p.getName(), p.getCode()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrainingPlanResponse updatePlan(Long planId, TrainingPlanUpdateRequest request) {
        // 1. Tìm bản ghi
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch ID: " + planId));

        // 2. Validate Trạng thái: Chỉ chặn sửa khi đang chờ duyệt.
        // Nếu là DRAFT hoặc REJECTED thì sửa thoải mái.
        // Nếu là APPROVED (trường hợp sửa đổi bổ sung) thì dùng hàm riêng.
        if (plan.getStatus() == ReportStatus.WAITING_SV || plan.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new IllegalStateException("Không thể chỉnh sửa khi đang chờ duyệt.");
        }

        // 3. Cập nhật Header (Title, Note...)
        mapper.updateHeader(plan, request);

        // 4. Cập nhật Details tùy theo trạng thái
        if (ReportStatus.APPROVED.equals(plan.getStatus())) {
            updateDetailsForApproved(plan, request);
        } else {
            updateDetailsForDraft(plan, request);
        }

        // 5. Lưu và trả về
        TrainingPlan savedPlan = trainingPlanRepository.save(plan);
        return mapper.toResponse(savedPlan);
    }

    // --- LOGIC CHO DRAFT/REJECTED (Xóa cũ, tạo mới hoàn toàn) ---
    private void updateDetailsForDraft(TrainingPlan plan, TrainingPlanUpdateRequest request) {
        if (request.getDetails() == null) return;

        // Quan trọng: clear() sẽ kích hoạt orphanRemoval=true để xóa row cũ trong DB
        plan.getDetails().clear();

        Long currentGroupId = plan.getGroup().getId();

        for (TrainingPlanDetailRequest rowRequest : request.getDetails()) {
            // Validate Employee & Process 1 lần cho mỗi Row (tối ưu hơn)
            Employee employee = getValidatedEmployee(rowRequest.getEmployeeId());
            Process process = getValidatedProcess(rowRequest.getProcessId(), currentGroupId);

            if (rowRequest.getSchedules() != null) {
                for (ScheduleRequest schedule : rowRequest.getSchedules()) {
                    // Chỉ lưu những ngày có tích chọn (plannedDay > 0)
                    if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {

                        TrainingPlanDetail detail = createBaseDetail(plan, employee, process, rowRequest.getNote(), schedule);

                        // Với Draft, trạng thái detail mặc định là OPEN hoặc PENDING
                        detail.setStatus(TrainingPlanDetailStatus.PENDING);

                        plan.getDetails().add(detail);
                    }
                }
            }
        }
    }

    private void updateDetailsForApproved(TrainingPlan plan, TrainingPlanUpdateRequest request) {
        if (request.getDetails() == null) return;

        // 1. Lưu lại danh sách cũ để đối chiếu trước khi clear
        List<TrainingPlanDetail> oldDetails = new ArrayList<>(plan.getDetails());
        plan.getDetails().clear();

        for (TrainingPlanDetailRequest rowRequest : request.getDetails()) {
            Employee employee = getValidatedEmployee(rowRequest.getEmployeeId());
            Process process = getValidatedProcess(rowRequest.getProcessId(), plan.getGroup().getId());

            if (rowRequest.getSchedules() != null) {
                for (ScheduleRequest schedule : rowRequest.getSchedules()) {
                    // Lấy trực tiếp ngày người dùng nhập từ Request
                    LocalDate requestDate = LocalDate.ofEpochDay(schedule.getPlannedDay());
                    if (requestDate == null) continue;

                    // Tìm xem trong DB đã có dòng này chưa (trùng Nhân viên, Quy trình và Ngày)
                    Optional<TrainingPlanDetail> existing = oldDetails.stream()
                            .filter(d -> d.getEmployee().getId().equals(employee.getId())
                                    && d.getProcess().getId().equals(process.getId())
                                    && d.getPlannedDate().equals(requestDate))
                            .findFirst();

                    if (existing.isPresent()) {
                        plan.getDetails().add(existing.get());
                        oldDetails.remove(existing.get()); // Xóa khỏi danh sách chờ xử lý "Nghỉ"
                    } else {
                        // Tạo mới hoàn toàn nếu là ngày mới người dùng vừa thêm
                        plan.getDetails().add(createBaseDetail(plan, employee, process, rowRequest.getNote(), schedule));
                    }
                }
            }
        }

        // 2. XỬ LÝ NHỮNG DÒNG BỊ "BỎ RƠI" (Dấu vết của việc dời lịch hoặc nghỉ)
        for (TrainingPlanDetail old : oldDetails) {
            // Nếu ngày cũ đã qua (hoặc là hôm nay) mà chưa hề có ngày thực tế (chưa ký đủ)
            if (!old.getPlannedDate().isAfter(LocalDate.now()) && old.getActualDate() == null) {
                old.setNote("Nghỉ"); // Đánh dấu nghỉ cho ngày không được thực hiện
                plan.getDetails().add(old); // Giữ lại trong DB để làm bằng chứng
            }
        }
    }

// --- CÁC HÀM HELPER (Giữ nguyên logic của bạn nhưng gọn hơn) ---

    private Employee getValidatedEmployee(Long employeeId) {
        // Nên dùng getReferenceById nếu chỉ cần gán quan hệ (lazy), nhưng findById an toàn hơn để check tồn tại
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Nhân viên ID " + employeeId + " không tồn tại"));
    }

    private Process getValidatedProcess(Long processId, Long groupId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new EntityNotFoundException("Công đoạn ID " + processId + " không tồn tại"));

        // Check xem Process có thuộc Group của Plan không (Chặn sai sót data)
        if (process.getGroup() == null || !process.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Công đoạn '" + process.getName() + "' không thuộc dây chuyền này.");
        }
        return process;
    }

    private TrainingPlanDetail createBaseDetail(TrainingPlan plan, Employee employee, Process process, String note, ScheduleRequest schedule) {
        TrainingPlanDetail detailEntity = new TrainingPlanDetail();

        // Gán các quan hệ
        detailEntity.setTrainingPlan(plan);
        detailEntity.setEmployee(employee);
        detailEntity.setProcess(process);

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

        validatePlanForSubmission(plan);

        plan.setFormCode(ReportUtils.generateFormCode(ApprovalEntityType.TRAINING_PLAN, plan.getGroup().getName(), planId));

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
            throw new BusinessException("Chỉ người tạo mới có thể sửa lại kế hoạch này.");
        }
        approvalService.revise(report, currentUser, request);
        trainingPlanRepository.save(report);
    }

    @Override
    @Transactional
    public void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        TrainingPlan report = getReportById(reportId);
        approvalService.approve(report, currentUser, req, request);
        if (report.getStatus() == ReportStatus.APPROVED) {
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

    // private methods
    private void validatePlanForSubmission(TrainingPlan plan) {
        // Business rules specific to TrainingPlan
        if (plan.getDetails() == null || plan.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Kế hoạch chưa có nội dung chi tiết. " +
                    "Vui lòng nhập ít nhất 1 dòng chi tiết trước khi gửi duyệt.");
        }

        if (plan.getTitle() == null || plan.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề kế hoạch không được để trống.");
        }

        // Validate date range
        if (plan.getMonthEnd().isBefore(plan.getMonthStart())) {
            throw new IllegalArgumentException("Tháng kết thúc không được nhỏ hơn tháng bắt đầu.");
        }

        // Validate details have required fields
        for (TrainingPlanDetail detail : plan.getDetails()) {
            if (detail.getEmployee() == null) {
                throw new IllegalArgumentException("Detail thiếu thông tin nhân viên.");
            }
            if (detail.getProcess() == null) {
                throw new IllegalArgumentException("Detail thiếu thông tin công đoạn.");
            }
            if (detail.getPlannedDate() == null) {
                throw new IllegalArgumentException("Detail thiếu ngày dự kiến.");
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
                .monthStart(plan.getMonthStart())
                .monthEnd(plan.getMonthEnd())
                .note(plan.getNote())
                .recordedAt(LocalDateTime.now())
                .groupId(plan.getGroup() != null ? plan.getGroup().getId() : null)
                .groupName(plan.getGroup() != null ? plan.getGroup().getName() : null)
                .detailHistories(new ArrayList<>())
                .build();

        // 2. Map Details (TrainingPlanDetail -> TrainingPlanDetailHistory)
        if (plan.getDetails() != null) {
            for (TrainingPlanDetail detail : plan.getDetails()) {

                TrainingPlanDetailHistory detailHistory = TrainingPlanDetailHistory.builder()
                        .trainingPlanHistory(history)
                        .employeeId(detail.getEmployee().getId())
                        .employeeCode(detail.getEmployee().getEmployeeCode())
                        .employeeName(detail.getEmployee().getFullName())
                        .processId(detail.getProcess().getId())
                        .processCode(detail.getProcess().getCode())
                        .processName(detail.getProcess().getName())
                        .targetMonth(detail.getTargetMonth())
                        .plannedDate(detail.getPlannedDate())
                        .actualDate(detail.getActualDate())
                        .status(detail.getStatus().toString())
                        .note(detail.getNote())
                        .build();

                history.getDetailHistories().add(detailHistory);
            }
        }

        trainingPlanHistoryRepository.save(history);
    }
}