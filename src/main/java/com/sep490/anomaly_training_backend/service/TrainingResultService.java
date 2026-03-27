package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillCertificateResponse;
import com.sep490.anomaly_training_backend.dto.response.EmployeeTrainingHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.KpiSummaryResponse;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.SampleResultResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultProductOptionResponse;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TrainingResultService {
    void generateTrainingResult(Long planId);

    KpiSummaryResponse getKpiSummary(Long teamId, Long lineId, Integer year);

    List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId);

    List<TrainingResultOptionResponse> getTrainingTopicsByProcess(Long processId);

    void updateResult(UpdateTrainingResultRequest request);

    void signDetailsByFi(List<FiSignRequest> requests);

    List<TrainingResultListResponse> getAllTrainingResults(User currentUser, Long lineId);

    List<TrainingResultListResponse> getResultsByLine(Long lineId);

    List<ProductLineResponse> getMyProductLines();

    TrainingResultDetailResponse getTrainingResultDetail(Long id);

    EmployeeTrainingHistoryResponse getEmployeeTrainingHistory(Long employeeId);

    TrainingResultDetailResponse getTrainingResultDetailForVerify(Long id);

    void submitResult(Long resultId);

    List<TrainingResultOptionResponse> getProcessesByLine(Long lineId);

    List<TrainingResultProcessResponse> getProcessesByEmployeeSkill(Long employeeId, Long lineId);

    List<TrainingResultProductOptionResponse> getProductsByProcess(Long processId);

    List<SampleResultResponse> getSamplesByProduct(Long productId);

    void rejectDetail(Long detailId, String reason);

    void reviseDetail(Long detailId);

    void retrainDetail(Long detailId);

    List<PrioritizedEmployeeResponse> getEmployeesInTeams(Long resultId);

    List<EmployeeSkillCertificateResponse> getSkillCertificates(Long resultId);

    // Relate approval methods
    void submit(Long reportId, User currentUser, HttpServletRequest request);

    void revise(Long reportId, User currentUser, HttpServletRequest request);

    void approveDetail(Long reportId, Long detailId, ApproveRequest req, User currentUser, HttpServletRequest request);

    void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void rejectDetail(Long reportId, Long detailId, RejectRequest req, User currentUser, HttpServletRequest request);

    void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request);

    void saveFeedback(Long detailId, DetailFeedbackRequest request, User currentUser);

    boolean canApprove(Long reportId, User currentUser);
}
