package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanRequest;
import com.sep490.anomaly_training_backend.dto.response.ApprovalLogDto;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanApproval;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TrainingPlanMapper {

    @Autowired
    protected GroupRepository groupRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected EmployeeRepository employeeRepository;
    @Autowired
    protected ProcessRepository processRepository;

    // =========================================================================
    // 1. MAP REQUEST -> ENTITY (CREATE / UPDATE)
    // =========================================================================

    // Dùng cho trường hợp Tạo mới (Create)
    @Mapping(target = "group", source = "groupId", qualifiedByName = "mapGroup")
    @Mapping(target = "details", ignore = true)      // Xử lý riêng ở Service
    @Mapping(target = "approvalLogs", ignore = true) // Log được tạo riêng, không map từ request
    @Mapping(target = "status", ignore = true)       // Status do Service quản lý
    @Mapping(target = "currentVersion", ignore = true)
    @Mapping(target = "lastRejectReason", ignore = true)
    public abstract TrainingPlan toEntity(TrainingPlanRequest request);

    // Dùng cho trường hợp Cập nhật (Update existing entity)
    @Mapping(target = "group", source = "groupId", qualifiedByName = "mapGroup")
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvalLogs", ignore = true)
    @Mapping(target = "lastRejectReason", ignore = true)
    // LƯU Ý: Đã xóa verifiedBySv và approvedByManager vì Entity mới không còn cột này
    public abstract void updateTrainingPlanFromRequest(TrainingPlanRequest request, @MappingTarget TrainingPlan entity);


    // =========================================================================
    // 2. MAP ENTITY -> RESPONSE (GET API)
    // =========================================================================

    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "approvalLogs", source = "approvalLogs") // Map list logs tự động nhờ hàm toLogDto
    public abstract TrainingPlanResponse toResponse(TrainingPlan entity);


    // =========================================================================
    // 3. MAP SUB-ENTITIES (DETAIL & LOGS)
    // =========================================================================

    // 3.1 Map Detail Request -> Entity
// CÁCH GỌN GÀNG HƠN (Khuyên dùng):
    @Mapping(target = "employee", source = "employeeId", qualifiedByName = "mapEmployee")
    @Mapping(target = "process", source = "processId", qualifiedByName = "mapProcess")
    @Mapping(target = "trainingPlan", ignore = true)
    @Mapping(target = "status", ignore = true)
    public abstract TrainingPlanDetail toDetailEntity(TrainingPlanDetailRequest request);

    // 3.2 Map Detail Entity -> Response
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "processName", source = "process.name")
    public abstract TrainingPlanDetailResponse toDetailResponse(TrainingPlanDetail entity);

    // 3.3 Map Approval Log Entity -> DTO (MỚI)
    @Mapping(target = "processedByName", source = "processedBy.fullName")
    @Mapping(target = "processedRole", source = "processedRole")
    public abstract ApprovalLogDto toLogDto(TrainingPlanApproval entity);


    // =========================================================================
    // 4. HELPER LOOKUP (Giữ nguyên logic cũ)
    // =========================================================================
    @Named("mapGroup")
    protected Group mapGroup(Long id) {
        if (id == null) return null;
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found ID: " + id));
    }

    @Named("mapEmployee")
    protected Employee mapEmployee(Long id) {
        if (id == null) return null;
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found ID: " + id));
    }

    @Named("mapProcess")
    protected Process mapProcess(Long id) {
        if (id == null) return null;
        return processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found ID: " + id));
    }

}