package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.service.ActionItemsService;
import com.sep490.anomaly_training_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionItemsServiceImpl implements ActionItemsService {

    private static final int EXPIRING_DAYS_THRESHOLD = 15;

    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    @Override
    public PendingSignatureResponse getPendingSignatures(Long lineId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        List<TrainingResultDetail> details;
        String signatureType;

        if (currentUser.hasRole("ROLE_FINAL_INSPECTION")) {
            details = trainingResultDetailRepository.findPendingFiOutSignatures(lineId);
            signatureType = "FI_OUT";
        } else if (currentUser.hasRole("ROLE_SUPERVISOR")) {
            details = trainingResultDetailRepository.findPendingSvReview(lineId);
            signatureType = "SV";
        } else {
            // Default: Team Lead → PRO_OUT
            details = trainingResultDetailRepository.findPendingProOutSignatures(lineId);
            signatureType = "PRO_OUT";
        }

        List<Long> resultIds = details.stream()
                .map(d -> d.getTrainingResult().getId())
                .distinct()
                .collect(Collectors.toList());

        List<String> employeeCodes = details.stream()
                .map(d -> d.getEmployee().getEmployeeCode())
                .distinct()
                .collect(Collectors.toList());

        String description;
        if ("PRO_OUT".equals(signatureType)) {
            description = "Kết test xong, chờ TL Sản xuất xác nhận";
        } else if ("FI_OUT".equals(signatureType)) {
            description = "Kết test xong, chờ Final Inspection xác nhận";
        } else {
            description = "Kết quả chờ Supervisor xác nhận";
        }

        return PendingSignatureResponse.builder()
                .count(details.size())
                .signatureType(signatureType)
                .description(description)
                .resultIds(resultIds)
                .employeeCodes(employeeCodes)
                .build();
    }

    @Override
    public FailedTrainingResponse getFailedTrainings(Long lineId) {
        List<TrainingResultDetail> details = trainingResultDetailRepository.findFailedTrainings(lineId);

        List<Long> resultIds = details.stream()
                .map(d -> d.getTrainingResult().getId())
                .distinct()
                .collect(Collectors.toList());

        List<String> employeeCodes = details.stream()
                .map(d -> d.getEmployee().getEmployeeCode())
                .distinct()
                .collect(Collectors.toList());

        String description = employeeCodes.isEmpty()
                ? "Không có nhân viên trượt"
                : String.join(", ", employeeCodes) + " chưa có lịch tái huấn luyện";

        return FailedTrainingResponse.builder()
                .count(details.size())
                .description(description)
                .resultIds(resultIds)
                .employeeCodes(employeeCodes)
                .build();
    }

    @Override
    public ExpiringSkillResponse getExpiringSkills(Long lineId) {
        List<EmployeeSkill> skills = employeeSkillRepository.findPendingReviewSkills(lineId);

        List<String> employeeCodes = skills.stream()
                .map(s -> s.getEmployee().getEmployeeCode())
                .distinct()
                .collect(Collectors.toList());

        String description = skills.isEmpty()
                ? "Không có chứng chỉ cần giám sát"
                : skills.size() + " chứng chỉ cần giám sát";

        return ExpiringSkillResponse.builder()
                .count(skills.size())
                .daysThreshold(0)
                .description(description)
                .employeeCodes(employeeCodes)
                .build();
    }
}
