package com.denso.anomaly_training_backend.service.impl;

import com.denso.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.denso.anomaly_training_backend.dto.request.TrainingPlanRequest;
import com.denso.anomaly_training_backend.dto.response.TrainingPlanInitDataResponse;
import com.denso.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.denso.anomaly_training_backend.enums.ApprovalAction;
import com.denso.anomaly_training_backend.enums.TrainingPlanStatus;
import com.denso.anomaly_training_backend.mapper.MasterDataTrainingPlanMapper;
import com.denso.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.denso.anomaly_training_backend.model.*;
import com.denso.anomaly_training_backend.model.Process;
import com.denso.anomaly_training_backend.repository.*;
import com.denso.anomaly_training_backend.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final GroupRepository groupRepository;
    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
    private final MasterDataTrainingPlanMapper masterDataMapper;
    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingPlanMapper trainingPlanMapper;
    private final UserRepository userRepository;
    private final TrainingPlanApprovalRepository approvalRepository;

    @Override
    @Transactional(readOnly = true)
    public TrainingPlanInitDataResponse getInitializationData(Long groupId) {
        // 1. Validate Group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 2. Lấy danh sách nhân viên:
        // Logic: Lấy tất cả nhân viên thuộc các Team của Group này và đang ACTIVE
        List<Employee> employees = employeeRepository.findAllActiveByGroupId(
                groupId
        );

        // 3. Lấy danh sách công đoạn
        List<Process> processes = processRepository.findByGroup_IdAndDeleteFlagFalse(groupId);

        // 4. Map & Return
        return TrainingPlanInitDataResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .availableEmployees(masterDataMapper.toEmployeeResponseList(employees))
                .availableProcesses(masterDataMapper.toProcessResponseList(processes))
                .build();
    }
    // ==================================================================================
    // 2. SAVE DRAFT
    // ==================================================================================
    @Override
    @Transactional
    public Long saveDraft(TrainingPlanRequest request) {
        // B1: Tạo hoặc Update thông tin chung (Header)
        TrainingPlan plan = createOrUpdateHeader(request);

        // B2: Cập nhật chi tiết (Matrix)
        updateDetails(plan, request.getDetails());

        // B3: Set cứng trạng thái DRAFT
        plan.setStatus(TrainingPlanStatus.DRAFT);

        // Save
        TrainingPlan savedPlan = trainingPlanRepository.save(plan);
        return savedPlan.getId();
    }

    // ==================================================================================
    // 3. SUBMIT PLAN
    // ==================================================================================
    @Override
    @Transactional
    public Long submitPlan(TrainingPlanRequest request) {
        TrainingPlan plan = createOrUpdateHeader(request);

        updateDetails(plan, request.getDetails());

        if (request.getSupervisorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supervisor ID is required for submission");
        }
        User supervisor = userRepository.findById(request.getSupervisorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supervisor not found"));

        plan.setStatus(TrainingPlanStatus.WAITING_SV);
        plan.setLastRejectReason(null);

        TrainingPlan savedPlan = trainingPlanRepository.save(plan);

        createApprovalLog(savedPlan, supervisor, ApprovalAction.SUBMIT, TrainingPlanStatus.WAITING_SV,
                "Gửi kế hoạch cho: " + supervisor.getFullName());

        return savedPlan.getId();
    }

    // ==================================================================================
    // 4. GET DETAIL
    // ==================================================================================
    @Override
    @Transactional(readOnly = true)
    public TrainingPlanResponse getTrainingPlanById(Long id) {
        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training Plan not found ID: " + id));
        return trainingPlanMapper.toResponse(plan);
    }

    /**
     * Xử lý tạo mới hoặc cập nhật thông tin Header (Title, Date, Group...)
     */
    private TrainingPlan createOrUpdateHeader(TrainingPlanRequest request) {
        TrainingPlan plan;
        if (request.getId() != null) {
            // Update
            plan = trainingPlanRepository.findById(request.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found for update"));
            trainingPlanMapper.updateTrainingPlanFromRequest(request, plan);
        } else {
            plan = trainingPlanMapper.toEntity(request);
            plan.setCurrentVersion(1); // Set default version
        }
        return plan;
    }

    /**
     * Xử lý danh sách chi tiết (One-to-Many).
     * Chiến lược: Xóa hết danh sách cũ và thêm mới (Clear & Add) để đơn giản hóa logic Matrix.
     * Vì JPA có orphanRemoval = true, việc clear list sẽ tự động xóa row trong DB.
     */
    private void updateDetails(TrainingPlan plan, List<TrainingPlanDetailRequest> detailRequests) {
        if (detailRequests == null) return;

        // Xóa danh sách cũ
        plan.getDetails().clear();

        // Map và thêm danh sách mới
        for (TrainingPlanDetailRequest detailReq : detailRequests) {
            // Dùng Mapper để convert từng dòng request sang entity
            TrainingPlanDetail detailEntity = trainingPlanMapper.toDetailEntity(detailReq);

            detailEntity.setTrainingPlan(plan);

            // Add vào list của parent
            plan.getDetails().add(detailEntity);
        }
    }

    /**
     * Tạo bản ghi log vào bảng training_plan_approval
     */
    private void createApprovalLog(TrainingPlan plan, User user, ApprovalAction action,
                                   TrainingPlanStatus status, String comment) {
        TrainingPlanApproval logEntry = TrainingPlanApproval.builder()
                .trainingPlan(plan)
                .processedBy(user)
                .processedRole(user.getRole().toString())
                .action(action)
                .resultingStatus(status)
                .comment(comment)
                .planVersion(plan.getCurrentVersion())
                .build();
        approvalRepository.save(logEntry);
    }

}