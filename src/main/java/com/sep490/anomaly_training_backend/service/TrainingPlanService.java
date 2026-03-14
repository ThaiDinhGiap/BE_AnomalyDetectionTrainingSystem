package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanGenerationRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TrainingPlanService {

    TrainingPlanResponse createPlan(TrainingPlanCreateRequest request);

    TrainingPlanResponse getPlanDetail(Long id);

    List<TrainingPlanResponse> getAllPlans();

    List<TrainingPlanResponse> getRejectedPlans();

    TrainingPlanResponse updatePlan(Long id, TrainingPlanUpdateRequest request);

    void deletePlan(Long id);

    void deleteDetail(Long planId, Long detailId);

    List<GroupResponse> getMyManagedGroups();

    List<ProcessResponse> getProcessesByProductLine(Long productLineId);

    List<ProductLineResponse> getProductLinesByGroupId(Long groupId);

    TrainingPlanDetailResponse addDetail(Long planId, TrainingPlanDetailRequest request);

    TrainingPlanDetailResponse updateDetail(Long planId, Long detailId, TrainingPlanDetailRequest request);

    List<EmployeeResponse> getEmployeesNotInPlan(Long planId);

    List<EmployeeResponse> getEmployeesInTeams(Long planId);

    List<TrainingPlanResponse> generateTrainingPlans(TrainingPlanGenerationRequest request);

    // Relate approval methods
    void submitPlanForApproval(Long planId, User currentUser, HttpServletRequest request);

    void submit(Long reportId, User currentUser, HttpServletRequest request);

    void revise(Long reportId, User currentUser, HttpServletRequest request);

    void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request);

    boolean canApprove(Long reportId, User currentUser);
}