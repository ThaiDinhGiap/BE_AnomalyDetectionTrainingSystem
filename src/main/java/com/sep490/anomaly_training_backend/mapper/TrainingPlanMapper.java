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
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentVersion", constant = "1")
    @Mapping(target = "formCode", constant = "TR_PLAN")
    @Mapping(target = "line", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "note", ignore = true)
    public abstract TrainingPlan toEntity(TrainingPlanCreateRequest request);

    // --- 3. MAPPING RESPONSE (HIỂN THỊ RA) ---
    @Mapping(source = "line.id", target = "lineId")
    @Mapping(source = "line.name", target = "lineName")
    @Mapping(source = "line.group.id", target = "groupId")
    @Mapping(source = "line.group.name", target = "groupName")
    @Mapping(target = "groupedDetails", ignore = true) // Xử lý trong service
    public abstract TrainingPlanResponse toResponse(TrainingPlan entity);

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "employee.employeeCode", target = "employeeCode")
    @Mapping(target = "employeeProcesses", ignore = true) // Xử lý trong service
    public abstract TrainingPlanDetailResponse toDetailResponse(TrainingPlanDetail entity);

    public abstract List<TrainingPlanDetailResponse> toDetailResponseList(List<TrainingPlanDetail> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "line", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "monthStart", ignore = true)
    @Mapping(target = "monthEnd", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    @Mapping(target = "formCode", ignore = true)
    public abstract void updateHeader(@MappingTarget TrainingPlan entity, TrainingPlanUpdateRequest request);
}
