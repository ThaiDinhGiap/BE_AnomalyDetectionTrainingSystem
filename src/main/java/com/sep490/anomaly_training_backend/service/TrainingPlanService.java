package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanGenerationRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanGenerationResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TrainingPlanService {

    // ── Query ────────────────────────────────────────────────────────────────

    TrainingPlanGenerationResponse getPlanDetail(Long id);

    List<TrainingPlanGenerationResponse> getAllPlans(User currentUser, Long lineId);

    List<TrainingPlanGenerationResponse> getRejectedPlans(User currentUser);

    // ── Mutation ─────────────────────────────────────────────────────────────

    TrainingPlanGenerationResponse updatePlan(Long id, TrainingPlanUpdateRequest request);

    void deletePlan(Long id);

    void deleteDetail(Long planId, Long detailId);

    TrainingPlanDetailResponse addDetail(Long planId, TrainingPlanDetailRequest request);

    TrainingPlanDetailResponse updateDetail(Long planId, Long detailId, TrainingPlanDetailRequest request);

    // ── Generate ─────────────────────────────────────────────────────────────

    TrainingPlanGenerationResponse generateTrainingPlans(User currentUser, TrainingPlanGenerationRequest request);

    // ── Lookup ───────────────────────────────────────────────────────────────

//    List<GroupResponse> getMyManagedGroups();

    List<ProcessResponse> getProcessesByProductLine(Long productLineId);

    List<ProductLineResponse> getProductLinesByGroupId(Long groupId);

    List<PrioritizedEmployeeResponse> getEmployeesNotInPlan(Long planId);

    List<PrioritizedEmployeeResponse> getEmployeesInTeams(Long planId);

    // ── Approval workflow ────────────────────────────────────────────────────

    void submitPlanForApproval(Long planId, User currentUser, HttpServletRequest request);

    void submit(Long reportId, User currentUser, HttpServletRequest request);

    void revise(Long reportId, User currentUser, HttpServletRequest request);

    void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request);

    ResponseEntity<Boolean> canApprove(Long reportId, User currentUser);

//    void clearFeedback(Long proposalId);
}