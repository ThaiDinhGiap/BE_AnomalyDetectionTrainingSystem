package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.TrainingPlanCreateRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.model.TrainingPlan;

import java.util.List;

public interface TrainingPlanService {

    TrainingPlanResponse createPlan(TrainingPlanCreateRequest request);

    TrainingPlanResponse getPlanDetail(Long id);

    List<TrainingPlanResponse> getAllPlans();

    TrainingPlanResponse updatePlan(Long id, TrainingPlanUpdateRequest request);

    List<GroupResponse> getMyManagedGroups();

    List<ProcessResponse> getProcessesByGroup(Long groupId);

    void submitPlan(Long planId);
    List<ProcessResponse> getProcessesByGroup(Long groupId);
    void submitPlan(Long planId);
    public void revertToDraft(Long planId);
}