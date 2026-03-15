package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.service.ActionItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionItemsServiceImpl implements ActionItemsService {

    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    @Override
    public List<PendingSignatureResponse> getPendingSignatures() {
        List<TrainingResultDetail> details = trainingResultDetailRepository.findPendingSignatures();
        return details.stream()
                .map(detail -> PendingSignatureResponse.builder()
                        .detailId(detail.getId())
                        .employeeName(detail.getEmployee().getFullName())
                        .employeeCode(detail.getEmployee().getEmployeeCode())
                        .processName(detail.getProcess() != null ? detail.getProcess().getName() : null)
                        .productName(detail.getProduct() != null ? detail.getProduct().getName() : null)
                        .plannedDate(detail.getPlannedDate())
                        .resultTitle(detail.getTrainingResult().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<FailedTrainingResponse> getFailedTrainings() {
        List<TrainingResultDetail> details = trainingResultDetailRepository.findFailedTrainings();
        return details.stream()
                .map(detail -> FailedTrainingResponse.builder()
                        .detailId(detail.getId())
                        .employeeName(detail.getEmployee().getFullName())
                        .employeeCode(detail.getEmployee().getEmployeeCode())
                        .processName(detail.getProcess() != null ? detail.getProcess().getName() : null)
                        .productName(detail.getProduct() != null ? detail.getProduct().getName() : null)
                        .failedDate(detail.getActualDate())
                        .resultTitle(detail.getTrainingResult().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpiringSkillResponse> getExpiringSkills() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<EmployeeSkill> skills = employeeSkillRepository.findExpiringSkills(thirtyDaysFromNow);
        return skills.stream()
                .map(skill -> ExpiringSkillResponse.builder()
                        .skillId(skill.getId())
                        .employeeName(skill.getEmployee().getFullName())
                        .employeeCode(skill.getEmployee().getEmployeeCode())
                        .processName(skill.getProcess().getName())
                        .expiryDate(skill.getExpiryDate())
                        .build())
                .collect(Collectors.toList());
    }
}
