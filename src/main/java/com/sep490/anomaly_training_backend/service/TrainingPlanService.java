package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TrainingPlanService {

    TrainingPlanResponse createPlan(TrainingPlanCreateRequest request);

    TrainingPlanResponse getPlanDetail(Long id);

    List<TrainingPlanResponse> getAllPlans();

    TrainingPlanResponse updatePlan(Long id, TrainingPlanUpdateRequest request);

    List<GroupResponse> getMyManagedGroups();

    List<ProcessResponse> getProcessesByGroup(Long groupId);

    // Relate approval methods
    void submitPlanForApproval(Long planId, User currentUser, HttpServletRequest request);

    void submit(Long reportId, User currentUser, HttpServletRequest request);

    void revise(Long reportId, User currentUser, HttpServletRequest request);

    void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request);

    boolean canApprove(Long reportId, User currentUser);
}