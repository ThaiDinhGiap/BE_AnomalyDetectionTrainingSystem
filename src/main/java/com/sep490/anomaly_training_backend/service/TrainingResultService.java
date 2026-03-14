package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.*;
import com.sep490.anomaly_training_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TrainingResultService {
    void generateTrainingResult(Long planId);

    List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId);

    List<TrainingResultOptionResponse> getTrainingTopicsByProcess(Long processId);

    void updateResult(UpdateTrainingResultRequest request);

    void signDetailsByFi(List<FiSignRequest> requests);

    List<TrainingResultListResponse> getAllTrainingResults();

    List<TrainingResultListResponse> getResultsByLine(Long lineId);

    List<ProductLineResponse> getMyProductLines();

    TrainingResultDetailResponse getTrainingResultDetail(Long id);

    void submitResult(Long resultId);

    List<TrainingResultOptionResponse> getProcessesByLine(Long lineId);

    List<TrainingResultProcessResponse> getProcessesByEmployeeSkill(Long employeeId, Long lineId);

    List<TrainingResultProductOptionResponse> getProductsByProcess(Long processId);
    List<SampleResultResponse> getSamplesByProduct(Long productId);

    void rejectDetail(Long detailId, String reason);

    void retrainDetail(Long detailId);

    // Relate approval methods
    void submitDetailForApproval(Long resultId, User currentUser, HttpServletRequest request);

    void submit(Long reportId, User currentUser, HttpServletRequest request);

    void revise(Long reportId, User currentUser, HttpServletRequest request);

    void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request);

    void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request);

    boolean canApprove(Long reportId, User currentUser);
}
