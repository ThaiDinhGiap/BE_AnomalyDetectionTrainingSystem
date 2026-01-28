// src/main/java/com/sep490/anomaly_training_backend/service/impl/TrainingPlanServiceImpl.java
package com.sep490.anomaly_training_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep490.anomaly_training_backend.dto.request.*;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    @Transactional
    public TrainingPlanResponse createPlan(TrainingPlanCreateRequest request) {
        if (request.getMonthEnd().isBefore(request.getMonthStart())) {
            throw new IllegalArgumentException("Tháng kết thúc không được nhỏ hơn tháng bắt đầu");
        }

        // 2. Lấy User hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 3. Lấy tất cả các Team mà user này làm Leader
        List<Team> managedTeams = teamRepository.findAllByTeamLeaderId(currentUser.getId());

        if (managedTeams.isEmpty()) {
            throw new IllegalStateException("Bạn không phải là Team Lead của bất kỳ nhóm nào.");
        }

        // 4. KIỂM TRA QUYỀN: User có quản lý cái groupId mà họ gửi lên không?
        // Logic: Duyệt qua list team quản lý -> xem có team nào thuộc group đó không.
        Group selectedGroup = managedTeams.stream()
                .map(Team::getGroup) // Lấy ra list Group từ list Team
                .filter(g -> g.getId().equals(request.getGroupId())) // So sánh ID
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bạn không có quyền tạo kế hoạch cho Dây chuyền này (ID: " + request.getGroupId() + ")"));

        // 5. Map và Lưu
        TrainingPlan trainingPlan = mapper.toEntity(request);
        trainingPlan.setGroup(selectedGroup); // Gán group đã validate
        trainingPlan.setCreatedBy(currentUser.getUsername());

        // (Lưu ý: Nếu cần lưu cả Team, bạn phải bắt user chọn TeamId thay vì GroupId.
        // Ở đây ta theo nghiệp vụ chọn Group nên để Team null hoặc chọn team đầu tiên thuộc group đó)

        TrainingPlan savedPlan = trainingPlanRepository.save(trainingPlan);
        return mapper.toResponse(savedPlan);
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

        // 2. Validate Trạng thái chung
        if (plan.getStatus() == ReportStatus.WAITING_SV || plan.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new IllegalStateException("Không thể chỉnh sửa khi đang chờ duyệt.");
        }

        mapper.updateHeader(plan, request);

        if (ReportStatus.APPROVED.equals(plan.getStatus())) {
            updateDetailsForApproved(plan, request);
        } else {
            updateDetailsForDraft(plan, request);
        }

        // 5. Lưu và trả về
        TrainingPlan savedPlan = trainingPlanRepository.save(plan);
        return mapper.toResponse(savedPlan);
    }

    private void updateDetailsForDraft(TrainingPlan plan, TrainingPlanUpdateRequest request) {
        if (request.getDetails() == null) return;

        plan.getDetails().clear();
        Long currentGroupId = plan.getGroup().getId();

        for (TrainingPlanDetailRequest rowRequest : request.getDetails()) {
            Employee employee = getValidatedEmployee(rowRequest.getEmployeeId());
            Process process = getValidatedProcess(rowRequest.getProcessId(), currentGroupId);

            if (rowRequest.getSchedules() != null) {
                for (ScheduleRequest schedule : rowRequest.getSchedules()) {
                    if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {

                        TrainingPlanDetail detail = createBaseDetail(plan, employee, process, rowRequest.getNote(), schedule);

                        plan.getDetails().add(detail);
                    }
                }
            }
        }
    }

    private void updateDetailsForApproved(TrainingPlan plan, TrainingPlanUpdateRequest request) {
        if (request.getDetails() == null) return;

        plan.getDetails().clear();
        Long currentGroupId = plan.getGroup().getId();

        for (TrainingPlanDetailRequest rowRequest : request.getDetails()) {
            Employee employee = getValidatedEmployee(rowRequest.getEmployeeId());
            Process process = getValidatedProcess(rowRequest.getProcessId(), currentGroupId);

            if (rowRequest.getSchedules() != null) {
                for (ScheduleRequest schedule : rowRequest.getSchedules()) {
                    if (schedule.getPlannedDay() != null && schedule.getPlannedDay() > 0) {

                        TrainingPlanDetail detail = createBaseDetail(plan, employee, process, rowRequest.getNote(), schedule);

                        // APPROVED: Tự động điền ActualDate

                        plan.getDetails().add(detail);
                    }
                }
            }
        }
    }

// -------------------------------------------------------------------------
// CÁC HÀM PHỤ TRỢ (HELPER) ĐỂ TRÁNH LẶP CODE
// Giúp code gọn gàng, dễ đọc hơn
// -------------------------------------------------------------------------

    private Employee getValidatedEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Nhân viên ID " + employeeId + " không tồn tại"));
    }

    private Process getValidatedProcess(Long processId, Long groupId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new EntityNotFoundException("Công đoạn ID " + processId + " không tồn tại"));

        if (!process.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Công đoạn '" + process.getName() + "' không thuộc dây chuyền này.");
        }
        return process;
    }

    // Hàm tạo object detail cơ bản (dùng chung cho cả 2 logic)
    private TrainingPlanDetail createBaseDetail(TrainingPlan plan, Employee employee, Process process, String note, ScheduleRequest schedule) {
        TrainingPlanDetail detailEntity = new TrainingPlanDetail();
        detailEntity.setTrainingPlan(plan);
        detailEntity.setEmployee(employee);
        detailEntity.setProcess(process);
        detailEntity.setNote(note);

        LocalDate targetMonth = schedule.getTargetMonth().withDayOfMonth(1);
        detailEntity.setTargetMonth(targetMonth);

        try {
            LocalDate plannedDate = targetMonth.withDayOfMonth(schedule.getPlannedDay());
            detailEntity.setPlannedDate(plannedDate);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Ngày " + schedule.getPlannedDay() +
                    " không hợp lệ trong tháng " + targetMonth.getMonthValue());
        }

        return detailEntity;
    }

    @Override
    @Transactional
    public void submitPlan(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch ID: " + planId));


        if (plan.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalStateException("Kế hoạch đang ở trạng thái " + plan.getStatus() + ", không thể gửi duyệt.");
        }

        if (plan.getDetails() == null || plan.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Kế hoạch chưa có nội dung chi tiết, vui lòng nhập liệu trước khi gửi.");
        }

        if (plan.getTitle() == null || plan.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề kế hoạch không được để trống.");
        }

        plan.setStatus(ReportStatus.WAITING_SV);

        // (Tuỳ chọn) Lưu log lịch sử, set ngày gửi...
        // plan.setSubmittedAt(LocalDateTime.now());

        // 5. Lưu
        trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void revertToDraft(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch ID: " + planId));

        if (plan.getStatus() != ReportStatus.REJECTED_BY_SV || plan.getStatus() != ReportStatus.REJECTED_BY_MANAGER) {
            throw new IllegalStateException("Chỉ có thể chuyển về bản nháp khi kế hoạch đã bị từ chối (REJECTED).");
        }

        createHistorySnapshot(plan);

        int currentVersion = (plan.getCurrentVersion() == null) ? 1 : plan.getCurrentVersion();
        plan.setCurrentVersion(currentVersion + 1);

        plan.setStatus(ReportStatus.DRAFT);

        // plan.setNote("");

        trainingPlanRepository.save(plan);
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