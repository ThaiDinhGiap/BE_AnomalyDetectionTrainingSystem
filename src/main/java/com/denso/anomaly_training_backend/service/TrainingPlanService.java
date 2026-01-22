package com.denso.anomaly_training_backend.service;

import com.denso.anomaly_training_backend.dto.request.TrainingPlanRequest;
import com.denso.anomaly_training_backend.dto.response.TrainingPlanInitDataResponse;
import com.denso.anomaly_training_backend.dto.response.TrainingPlanResponse;

public interface TrainingPlanService {

    // Lấy dữ liệu khởi tạo (Employees, Processes) để vẽ bảng
    TrainingPlanInitDataResponse getInitializationData(Long groupId);

    // Lưu nháp (Không validate supervisor, status = DRAFT)
    Long saveDraft(TrainingPlanRequest request);

    // Gửi duyệt (Validate supervisor, status = WAITING_SV, ghi log)
    Long submitPlan(TrainingPlanRequest request);

    // Lấy chi tiết + Lịch sử duyệt
    TrainingPlanResponse getTrainingPlanById(Long id);
}