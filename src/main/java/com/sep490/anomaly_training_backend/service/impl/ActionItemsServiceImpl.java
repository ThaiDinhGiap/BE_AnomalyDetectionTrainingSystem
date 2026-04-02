package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.ActionItemsService;
import com.sep490.anomaly_training_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionItemsServiceImpl implements ActionItemsService {

    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final ProductLineRepository productLineRepository;
    private final GroupRepository groupRepository;
    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;

    /**
     * Resolve danh sách lineIds liên quan tới user hiện tại theo permission.
     * - review_approve.confirm (FI): teams mà FI phụ trách → group → productLine
     * - group.manage (SV): groups mà SV quản lý → productLine
     * - section.manage (MNG): sections mà MNG quản lý → productLine
     * - team.manage (TL): teams mà TL quản lý → group → productLine
     */
    private List<Long> resolveUserLineIds(User currentUser) {
        if (currentUser.hasPermission("section.manage")) {
            return sectionRepository.findByManagerId(currentUser.getId()).stream()
                    .flatMap(s -> productLineRepository.findBySection(s.getId()).stream())
                    .map(ProductLine::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (currentUser.hasPermission("group.manage")) {
            return groupRepository.findBySupervisorId(currentUser.getId()).stream()
                    .flatMap(g -> productLineRepository.findByGroupIdAndDeleteFlagFalse(g.getId()).stream())
                    .map(ProductLine::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (currentUser.hasPermission("review_approve.confirm")) {
            return teamRepository.findByFinalInspectionId(currentUser.getId()).stream()
                    .map(Team::getGroup)
                    .filter(Objects::nonNull)
                    .map(Group::getId)
                    .distinct()
                    .flatMap(groupId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(groupId).stream())
                    .map(ProductLine::getId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        // Default: team.manage (TL)
        return teamRepository.findAllByTeamLeaderId(currentUser.getId()).stream()
                .map(Team::getGroup)
                .filter(Objects::nonNull)
                .map(Group::getId)
                .distinct()
                .flatMap(groupId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(groupId).stream())
                .map(ProductLine::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public PendingSignatureResponse getPendingSignatures(Long lineId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        List<TrainingResultDetail> details;
        String signatureType;

        if (currentUser.hasPermission("review_approve.confirm")) {
            details = lineId != null
                    ? trainingResultDetailRepository.findPendingFiOutSignatures(lineId)
                    : resolveAndQuery(currentUser, "FI_OUT");
            signatureType = "FI_OUT";
        } else if (currentUser.hasPermission("group.manage")) {
            details = lineId != null
                    ? trainingResultDetailRepository.findPendingSvReview(lineId)
                    : resolveAndQuery(currentUser, "SV");
            signatureType = "SV";
        } else {
            details = lineId != null
                    ? trainingResultDetailRepository.findPendingProOutSignatures(lineId)
                    : resolveAndQuery(currentUser, "PRO_OUT");
            signatureType = "PRO_OUT";
        }

        return buildPendingResponse(details, signatureType);
    }

    private List<TrainingResultDetail> resolveAndQuery(User currentUser, String signatureType) {
        List<Long> lineIds = resolveUserLineIds(currentUser);
        if (lineIds.isEmpty()) return List.of();

        return switch (signatureType) {
            case "FI_OUT" -> trainingResultDetailRepository.findPendingFiOutSignaturesByLineIds(lineIds);
            case "SV" -> trainingResultDetailRepository.findPendingSvReviewByLineIds(lineIds);
            default -> trainingResultDetailRepository.findPendingProOutSignaturesByLineIds(lineIds);
        };
    }

    private PendingSignatureResponse buildPendingResponse(List<TrainingResultDetail> details, String signatureType) {
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
        } else if ("SV".equals(signatureType)) {
            description = "Kết quả chờ Supervisor xác nhận";
        } else {
            description = details.isEmpty()
                    ? "Không có báo cáo nào đang chờ duyệt"
                    : details.size() + " báo cáo đã gửi, đang chờ cấp trên ký duyệt";
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
    public PendingSignatureResponse getSubmittedPendingApproval(Long lineId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();
        String username = currentUser.getUsername();

        List<TrainingResultDetail> details;
        if (lineId != null) {
            details = trainingResultDetailRepository.findSubmittedPendingApproval(username, lineId);
        } else {
            List<Long> lineIds = resolveUserLineIds(currentUser);
            if (lineIds.isEmpty()) {
                details = List.of();
            } else {
                details = trainingResultDetailRepository.findSubmittedPendingApproval(username, null)
                        .stream()
                        .filter(d -> d.getTrainingResult().getLine() != null
                                && lineIds.contains(d.getTrainingResult().getLine().getId()))
                        .collect(Collectors.toList());
            }
        }

        return buildPendingResponse(details, "SUBMITTED");
    }

    @Override
    public FailedTrainingResponse getFailedTrainings(Long lineId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        List<TrainingResultDetail> details;
        if (lineId != null) {
            details = trainingResultDetailRepository.findFailedTrainings(lineId);
        } else {
            List<Long> lineIds = resolveUserLineIds(currentUser);
            details = lineIds.isEmpty()
                    ? List.of()
                    : trainingResultDetailRepository.findFailedTrainingsByLineIds(lineIds);
        }

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
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        List<EmployeeSkill> skills;
        if (lineId != null) {
            skills = employeeSkillRepository.findPendingReviewSkills(lineId);
        } else {
            List<Long> lineIds = resolveUserLineIds(currentUser);
            skills = lineIds.isEmpty()
                    ? List.of()
                    : employeeSkillRepository.findPendingReviewSkillsByLineIds(lineIds);
        }

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
