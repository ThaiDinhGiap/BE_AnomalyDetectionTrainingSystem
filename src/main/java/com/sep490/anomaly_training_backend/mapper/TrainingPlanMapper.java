package com.sep490.anomaly_training_backend.mapper;

import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TrainingPlanMapper {

    // --- 1. MAPPING TẠO MỚI (CREATE) ---
    // Gộp tất cả logic vào đây
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)  // Will be set by builder default
    @Mapping(target = "currentVersion", constant = "1")
    @Mapping(target = "formCode", constant = "TR_PLAN")
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "line", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "note", ignore = true)
    public abstract TrainingPlan toEntity(TrainingPlanCreateRequest request);

    // --- 3. MAPPING RESPONSE (HIỂN THỊ RA) ---
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "line.id", target = "lineId")
    @Mapping(source = "line.name", target = "lineName")
    public abstract TrainingPlanResponse toResponse(TrainingPlan entity);

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "employee.employeeCode", target = "employeeCode")
    @Mapping(source = "process.id", target = "processId")
    @Mapping(source = "process.name", target = "processName")
    public abstract TrainingPlanDetailResponse toDetailResponse(TrainingPlanDetail entity);

    public abstract List<TrainingPlanDetailResponse> toDetailResponseList(List<TrainingPlanDetail> list);

    // 1. Map Update Header (Bỏ qua các trường không cho sửa hoặc tự xử lý)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)      // Không cho đổi Group
    @Mapping(target = "line", ignore = true)       // Không cho đổi Line
    @Mapping(target = "monthStart", ignore = true) // Không cho đổi tháng bắt đầu
    @Mapping(target = "monthEnd", ignore = true)   // Không cho đổi tháng kết thúc
    @Mapping(target = "status", ignore = true)     // Status xử lý riêng
    @Mapping(target = "details", ignore = true)    // Details xử lý riêng trong Service
    @Mapping(target = "currentVersion", ignore = true)
    @Mapping(target = "formCode", ignore = true)
    public abstract void updateHeader(@MappingTarget TrainingPlan entity, TrainingPlanUpdateRequest request);

}