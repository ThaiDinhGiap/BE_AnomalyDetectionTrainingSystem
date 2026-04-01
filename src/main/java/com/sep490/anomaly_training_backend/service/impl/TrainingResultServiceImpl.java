package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest;
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
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.PrioritySnapshotDetail;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.model.ProductProcess;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.TrainingResultDetailHistory;
import com.sep490.anomaly_training_backend.model.TrainingResultHistory;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotDetailRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.ProductProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultHistoryRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.util.ReportUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingResultServiceImpl implements TrainingResultService {

    private final TrainingResultRepository trainingResultRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final UserRepository userRepository;
    private final ApprovalService approvalService;
    private final TeamRepository teamRepository;
    private final ProductLineRepository productLineRepository;
    private final ProcessRepository processRepository;
    private final ProductRepository productRepository;
    private final TrainingSampleRepository trainingSampleRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final ProductProcessRepository productProcessRepository;
    private final EmployeeRepository employeeRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final PrioritySnapshotRepository prioritySnapshotRepository;
    private final PrioritySnapshotDetailRepository prioritySnapshotDetailRepository;
    private final GroupRepository groupRepository;
    private final RejectReasonRepository rejectReasonRepository;
    private final RequiredActionRepository requiredActionRepository;
    private final TrainingResultHistoryRepository trainingResultHistoryRepository;

    private static final int HISTORY_SIZE = 6;

    @Override
    public KpiSummaryResponse getKpiSummary(Long teamId, Long lineId, Integer year) {
        // Nếu không truyền filter nào → chỉ tính KPI cho báo cáo của current user
        String createdBy = null;
        if (teamId == null && lineId == null && year == null) {
            createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        long totalExecuted = trainingResultDetailRepository.countByFilters(createdBy, teamId, lineId, year);
        long totalPass = trainingResultDetailRepository.countByFiltersAndIsPass(createdBy, teamId, lineId, year, true);
        long totalFail = trainingResultDetailRepository.countByFiltersAndIsPass(createdBy, teamId, lineId, year, false);

        BigDecimal passRate = BigDecimal.ZERO;
        if (totalExecuted > 0) {
            passRate = BigDecimal.valueOf(totalPass)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalExecuted), 2, RoundingMode.HALF_UP);
        }

        return KpiSummaryResponse.builder()
                .totalExecuted(totalExecuted)
                .totalPass(totalPass)
                .totalFail(totalFail)
                .passRate(passRate)
                .build();
    }

    @Override
    public List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId) {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> new TrainingResultOptionResponse(p.getId(), p.getCode() + " - " + p.getName()))
                .toList();
    }

    @Override
    public List<TrainingResultOptionResponse> getTrainingTopicsByProcess(Long processId) {
        List<TrainingSample> samples = trainingSampleRepository.findByProcessIdOrderByCreatedAtDesc(processId);
        return samples.stream()
                .map(s -> new TrainingResultOptionResponse(s.getId(), s.getCategoryName()))
                .toList();
    }

    @Override
    @Transactional
    public void updateResult(UpdateTrainingResultRequest request) {
        if (request == null || request.getId() == null)
            return;

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TrainingResult header = trainingResultRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        if (request.getTitle() != null)
            header.setTitle(request.getTitle());
        if (request.getNote() != null)
            header.setNote(request.getNote());

        trainingResultRepository.save(header);

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<TrainingResultDetail> detailsToSave = new ArrayList<>();

            for (UpdateResultDetailRequest reqDetail : request.getDetails()) {
                TrainingResultDetail detail = trainingResultDetailRepository.findById(reqDetail.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

                if (reqDetail.getProcessId() != null) {
                    com.sep490.anomaly_training_backend.model.Process process = processRepository
                            .findById(reqDetail.getProcessId())
                            .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));
                    detail.setProcess(process);

                    if (reqDetail.getClassification() == null && process.getClassification() != null) {
                        detail.setClassification(process.getClassification().getValue());
                    }
                    if (reqDetail.getCycleTimeStandard() == null && process.getStandardTimeJt() != null) {
                        detail.setCycleTimeStandard(process.getStandardTimeJt());
                    }
                }

                if (reqDetail.getProductId() != null) {
                    Product product = productRepository.findById(reqDetail.getProductId())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                    detail.setProduct(product);
                }

                if (reqDetail.getTrainingSampleId() != null) {
                    TrainingSample sample = trainingSampleRepository.findById(reqDetail.getTrainingSampleId())
                            .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));
                    detail.setTrainingSample(sample);
                    if (reqDetail.getSampleCode() == null && sample.getTrainingSampleCode() != null) {
                        detail.setSampleCode(sample.getTrainingSampleCode());
                    }
                }

                if (reqDetail.getClassification() != null) {
                    detail.setClassification(reqDetail.getClassification());
                }
                if (reqDetail.getCycleTimeStandard() != null) {
                    detail.setCycleTimeStandard(reqDetail.getCycleTimeStandard());
                }
                if (reqDetail.getSampleCode() != null) {
                    detail.setSampleCode(reqDetail.getSampleCode());
                }
                if (reqDetail.getTrainingTopic() != null) {
                    detail.setTrainingTopic(reqDetail.getTrainingTopic());
                }
                if (reqDetail.getTimeIn() != null)
                    detail.setTimeIn(reqDetail.getTimeIn());
                if (reqDetail.getTimeStartOp() != null)
                    detail.setTimeStartOp(reqDetail.getTimeStartOp());
                if (reqDetail.getTimeOut() != null)
                    detail.setTimeOut(reqDetail.getTimeOut());
                if (reqDetail.getDetectionTime() != null)
                    detail.setDetectionTime(reqDetail.getDetectionTime());

                // Auto-calculate isPass if possible
                if (detail.getTimeIn() != null && detail.getTimeOut() != null
                        && detail.getCycleTimeStandard() != null) {
                    long actualSeconds = java.time.Duration.between(detail.getTimeIn(), detail.getTimeOut())
                            .toSeconds();
                    detail.setIsPass(actualSeconds <= detail.getCycleTimeStandard().longValue());
                }

                // Manual override from FE always takes precedence
                if (reqDetail.getIsPass() != null) {
                    detail.setIsPass(reqDetail.getIsPass());
                }

                if (reqDetail.getNote() != null)
                    detail.setNote(reqDetail.getNote());

                if (Boolean.TRUE.equals(reqDetail.getIsSignProIn())) {
                    if (detail.getSignatureProIn() == null) {
                        detail.setSignatureProIn(currentUser);
                    }
                }
                if (Boolean.TRUE.equals(reqDetail.getIsSignProOut())) {
                    if (detail.getSignatureProOut() == null) {
                        detail.setSignatureProOut(currentUser);
                    }
                }

                if (reqDetail.getIsRetrained() != null) {
                    detail.setIsRetrained(reqDetail.getIsRetrained());
                }

                // Khi isPass được xác định → điền actualDate cho cả result detail và plan
                // detail
                if (detail.getIsPass() != null && detail.getActualDate() == null) {
                    LocalDate today = LocalDate.now();
                    detail.setActualDate(today);

                    if (detail.getTrainingPlanDetail() != null
                            && detail.getTrainingPlanDetail().getActualDate() == null) {
                        detail.getTrainingPlanDetail().setActualDate(today);
                    }
                }

                detailsToSave.add(detail);
            }

            trainingResultDetailRepository.saveAll(detailsToSave);
        }
    }

    private boolean isFullSigned(TrainingResultDetail detail) {
        return detail.getSignatureProIn() != null &&
                detail.getSignatureProOut() != null &&
                detail.getSignatureFiIn() != null &&
                detail.getSignatureFiOut() != null;
    }

    /**
     * Auto-mark các detail quá hạn (plannedDate < today, chưa đánh giá) thành MISS.
     * Cập nhật cả TrainingResultDetail và TrainingPlanDetail.
     */
    private void markOverdueDetailsAsMiss(TrainingResult result) {
        LocalDate today = LocalDate.now();
        boolean hasChanges = false;

        for (TrainingResultDetail detail : result.getDetails()) {
            if (detail.getPlannedDate() != null
                    && detail.getPlannedDate().isBefore(today)
                    && detail.getIsPass() == null
                    && detail.getStatus() != ReportStatus.MISSED) {

                detail.setStatus(ReportStatus.MISSED);
                hasChanges = true;

                if (detail.getTrainingPlanDetail() != null
                        && detail.getTrainingPlanDetail()
                        .getStatus() != com.sep490.anomaly_training_backend.enums.ReportStatus.MISSED) {
                    detail.getTrainingPlanDetail().setStatus(
                            com.sep490.anomaly_training_backend.enums.ReportStatus.MISSED);
                }
            }
        }

        if (hasChanges) {
            trainingResultRepository.save(result);
        }
    }

    @Override
    @Transactional
    public void signDetailsByFi(List<FiSignRequest> requests) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!currentUser.hasPermission("review_approve.confirm")) {
            throw new AppException(ErrorCode.CONFIRM_PERMISSION_REQUIRED);
        }

        if (requests == null || requests.isEmpty())
            return;

        List<TrainingResultDetail> detailsToSave = new ArrayList<>();
        for (FiSignRequest req : requests) {
            TrainingResultDetail detail = trainingResultDetailRepository.findById(req.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

            if (Boolean.TRUE.equals(req.getIsSignIn())) {
                if (detail.getSignatureFiIn() == null) {
                    detail.setSignatureFiIn(currentUser);
                }
            }

            if (Boolean.TRUE.equals(req.getIsSignOut())) {
                if (detail.getSignatureFiOut() == null) {
                    detail.setSignatureFiOut(currentUser);
                }
            }
            detailsToSave.add(detail);
        }

        trainingResultDetailRepository.saveAll(detailsToSave);
    }

    @Override
    public List<TrainingResultListResponse> getAllTrainingResults(User currentUser, Long lineId) {

        List<TrainingResult> results = new ArrayList<>();

        if (currentUser.hasRole("ROLE_FINAL_INSPECTION")) {
            List<Team> teams = teamRepository.findByFinalInspectionId(currentUser.getId());
            if (teams.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> groupIds = teams.stream().map(team -> team.getGroup().getId()).distinct().toList();
            if (lineId != null) {
                results = trainingResultRepository.findAllByGroupIdsAndLineId(groupIds, lineId);
            } else {
                results = trainingResultRepository.findAllByGroupIds(groupIds);
            }
        } else {
            List<ReportStatus> excludedStatuses = Arrays.asList(ReportStatus.DRAFT, ReportStatus.REVISING);

            if (currentUser.hasPermission("section.manage")) {
                if (lineId != null) {
                    results = trainingResultRepository.findAllByManagerAndLineId(currentUser.getId(), lineId,
                            excludedStatuses);
                } else {
                    results = trainingResultRepository.findAllByManager(currentUser.getId(), excludedStatuses);
                }
            } else if (currentUser.hasPermission("group.manage")) {
                List<com.sep490.anomaly_training_backend.model.Group> groups = groupRepository
                        .findBySupervisorId(currentUser.getId());
                if (groups.isEmpty()) {
                    return Collections.emptyList();
                }
                List<Long> groupIds = groups.stream().map(com.sep490.anomaly_training_backend.model.Group::getId)
                        .distinct().toList();

                if (lineId != null) {
                    results = trainingResultRepository.findAllByGroupIdsAndLineId(groupIds, lineId);
                } else {
                    results = trainingResultRepository.findAllByGroupIds(groupIds);
                }
            } else if (currentUser.hasPermission("team.manage")) {
                if (lineId != null) {
                    results = trainingResultRepository.findAllByCreatedByAndLineId(currentUser.getUsername(), lineId);
                } else {
                    results = trainingResultRepository.findAllByCreatedBy(currentUser.getUsername());
                }
            }
        }
        return mapToListResponse(results);
    }

    @Override
    public List<TrainingResultListResponse> getResultsByLine(Long lineId) {
        List<TrainingResult> entities = trainingResultRepository.findByLineIdAndDeleteFlagFalse(lineId);
        return mapToListResponse(entities);
    }

    @Override
    public List<ProductLineResponse> getMyProductLines() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Team> teams = teamRepository.findByTeamLeader_Username(username)
                .map(List::of)
                .orElse(List.of());

        return teams.stream()
                .map(team -> team.getGroup().getId())
                .distinct()
                .flatMap(groupId -> productLineRepository.findByGroupIdAndDeleteFlagFalse(groupId).stream())
                .map(pl -> ProductLineResponse.builder()
                        .id(pl.getId())
                        .groupId(pl.getGroup().getId())
                        .name(pl.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TrainingResultListResponse> mapToListResponse(List<TrainingResult> entities) {
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        return entities.stream().map(entity -> {
            TrainingResultListResponse dto = new TrainingResultListResponse();
            dto.setId(entity.getId());
            dto.setTitle(entity.getTitle());
            dto.setCreatedAt(entity.getCreatedAt());

            if (entity.getLine() != null) {
                dto.setLineId(entity.getLine().getId());
                dto.setLineName(entity.getLine().getName());
            }

            if (entity.getStatus() != null) {
                dto.setStatus(entity.getStatus().toString());
            }

            if (entity.getCreatedBy() != null) {
                dto.setCreatedBy(entity.getCreatedBy());
            }

            TrainingPlan plan = entity.getTrainingPlan();
            if (plan != null && plan.getStartDate() != null && plan.getEndDate() != null) {
                List<String> months = new ArrayList<>();
                java.time.LocalDate cursor = plan.getStartDate().withDayOfMonth(1);
                java.time.LocalDate end = plan.getEndDate().withDayOfMonth(1);
                while (!cursor.isAfter(end)) {
                    months.add(cursor.format(monthYearFormatter));
                    cursor = cursor.plusMonths(1);
                }
                dto.setMonthList(String.join(", ", months));
            } else {
                dto.setMonthList("");
            }

            // Fetch details for the current TrainingResult to calculate progress statistics
            List<TrainingResultDetail> details = trainingResultDetailRepository.findByTrainingResultId(entity.getId());

            long totalItems = details.size();
            long totalPass = details.stream().filter(d -> d.getIsPass() != null && d.getIsPass()).count();
            long totalFail = details.stream().filter(d -> d.getIsPass() != null && !d.getIsPass()).count();
            long totalNotYetTrained = details.stream().filter(d -> d.getIsPass() == null).count();

            BigDecimal passRate = BigDecimal.ZERO;
            if (totalPass + totalFail > 0) { // Only calculate if there are any trained items
                passRate = BigDecimal.valueOf(totalPass)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalPass + totalFail), 2, RoundingMode.HALF_UP);
            }

            dto.setTotalItems(totalItems);
            dto.setTotalPass(totalPass);
            dto.setTotalFail(totalFail);
            dto.setTotalNotYetTrained(totalNotYetTrained);
            dto.setPassRate(passRate);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrainingResultDetailResponse getTrainingResultDetail(Long id) {
        TrainingResult result = trainingResultRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        markOverdueDetailsAsMiss(result);

        User user = userRepository.findByUsername(result.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TrainingResultDetailResponse response = buildHeaderResponse(result, user);

        List<TrainingResultDetailResponse.DetailRowDto> detailDtos = result.getDetails().stream()
                .map(this::mapDetailToRow)
                .sorted()
                .collect(Collectors.toList());

        response.setDetails(detailDtos);
        return response;
    }

    @Override
    public EmployeeTrainingHistoryResponse getEmployeeTrainingHistory(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        List<TrainingResultDetail> details = trainingResultDetailRepository.getTrainingHistory(employeeId);

        List<EmployeeTrainingHistoryResponse.TrainingHistoryItem> items = details.stream()
                .map(d -> {
                    TrainingResult result = d.getTrainingResult();
                    String planTitle = result != null && result.getTitle() != null ? result.getTitle() : "";
                    String teamLeadName = "";
                    if (result != null && result.getCreatedBy() != null) {
                        teamLeadName = userRepository.findByUsername(result.getCreatedBy())
                                .map(User::getFullName)
                                .orElse(result.getCreatedBy());
                    }

                    return EmployeeTrainingHistoryResponse.TrainingHistoryItem.builder()
                            .detailId(d.getId())
                            .planTitle(planTitle)
                            .teamLeadName(teamLeadName)
                            .actualDate(d.getActualDate())
                            .timeIn(d.getTimeIn())
                            .processId(d.getProcess() != null ? d.getProcess().getId() : null)
                            .processCode(d.getProcess() != null ? d.getProcess().getCode() : null)
                            .processName(d.getProcess() != null ? d.getProcess().getName() : null)
                            .trainingTopic(d.getTrainingTopic())
                            .note(d.getNote())
                            .isPass(d.getIsPass())
                            .build();
                })
                .collect(Collectors.toList());

        return EmployeeTrainingHistoryResponse.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .totalRecords(items.size())
                .histories(items)
                .build();
    }

    private static final Set<ReportStatus> SV_VISIBLE_STATUSES = Set.of(
            ReportStatus.PENDING_REVIEW,
            ReportStatus.REJECTED,
            ReportStatus.COMPLETED);

    private static final Set<ReportStatus> FI_VISIBLE_STATUSES = Set.of(
            ReportStatus.PENDING_CONFIRMATION,
            ReportStatus.PENDING_REVIEW,
            ReportStatus.REJECTED,
            ReportStatus.COMPLETED);
    ;

    @Override
    @Transactional
    public TrainingResultDetailResponse getTrainingResultDetailForVerify(User currentUser, Long id) {
        TrainingResult result = trainingResultRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        markOverdueDetailsAsMiss(result);

        User user = userRepository.findByUsername(result.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TrainingResultDetailResponse response = buildHeaderResponse(result, user);
        List<TrainingResultDetailResponse.DetailRowDto> detailRowDtos;

        if (currentUser.hasPermission("review_approve.confirm")) {
            detailRowDtos = result.getDetails().stream()
                    .filter(detail -> detail.getStatus() != null && FI_VISIBLE_STATUSES.contains(detail.getStatus()))
                    .map(this::mapDetailToRow)
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            detailRowDtos = result.getDetails().stream()
                    .filter(detail -> detail.getStatus() != null && SV_VISIBLE_STATUSES.contains(detail.getStatus()))
                    .map(this::mapDetailToRow)
                    .sorted()
                    .collect(Collectors.toList());
        }

        response.setDetails(detailRowDtos);
        return response;
    }

    /**
     * Build header response (thông tin chung của TrainingResult) — dùng chung cho
     * TL và SV.
     */
    private TrainingResultDetailResponse buildHeaderResponse(TrainingResult result, User user) {
        TrainingResultDetailResponse response = new TrainingResultDetailResponse();

        response.setId(result.getId());
        response.setTitle(result.getTitle());
        response.setStatus(result.getStatus() != null ? result.getStatus().toString() : null);
        response.setCurrentVersion(result.getCurrentVersion());
        response.setNote(result.getNote());
        response.setYear(result.getYear());
        response.setCreatedAt(result.getCreatedAt());
        response.setCreatedByName(user.getFullName());

        if (result.getLine() != null) {
            response.setLineId(result.getLine().getId());
            response.setLineName(result.getLine().getName());
            if (result.getLine().getGroup() != null) {
                response.setGroupId(result.getLine().getGroup().getId());
                response.setGroupName(result.getLine().getGroup().getName());
            }
        }

        if (result.getTrainingPlan() != null) {
            response.setTrainingPlanId(result.getTrainingPlan().getId());
            response.setTrainingPlanTitle(result.getTrainingPlan().getTitle());
        }

        return response;
    }

    /**
     * Map 1 TrainingResultDetail → DetailRowDto — dùng chung cho TL và SV.
     */
    private TrainingResultDetailResponse.DetailRowDto mapDetailToRow(TrainingResultDetail detail) {
        TrainingResultDetailResponse.DetailRowDto row = new TrainingResultDetailResponse.DetailRowDto();

        row.setId(detail.getId());
        row.setPlannedDate(detail.getPlannedDate());
        row.setActualDate(detail.getActualDate());
        row.setDetailStatus(detail.getStatus() != null ? detail.getStatus().toString() : null);

        TrainingPlanDetail planDetail = detail.getTrainingPlanDetail();
        if (planDetail != null) {
            row.setTrainingPlanDetailId(planDetail.getId());
            row.setBatchId(planDetail.getBatchId());

            if (planDetail.getEmployee() != null) {
                row.setEmployeeId(planDetail.getEmployee().getId());
                row.setEmployeeName(planDetail.getEmployee().getFullName());
                row.setEmployeeCode(planDetail.getEmployee().getEmployeeCode());
            }
        } else if (detail.getBatchId() != null) {
            row.setBatchId(detail.getBatchId());
        }

        if (detail.getEmployee() != null) {
            row.setEmployeeId(detail.getEmployee().getId());
            row.setEmployeeName(detail.getEmployee().getFullName());
            row.setEmployeeCode(detail.getEmployee().getEmployeeCode());
        }

        if (detail.getProcess() != null) {
            row.setProcessId(detail.getProcess().getId());
            row.setProcessCode(detail.getProcess().getCode());
            row.setProcessName(detail.getProcess().getName());
            if (detail.getClassification() != null) {
                row.setClassification(detail.getClassification());
            } else if (detail.getProcess().getClassification() != null) {
                row.setClassification(detail.getProcess().getClassification().getValue());
            }
            if (detail.getCycleTimeStandard() != null) {
                row.setStandardTime(detail.getCycleTimeStandard());
            } else if (detail.getProcess().getStandardTimeJt() != null) {
                row.setStandardTime(detail.getProcess().getStandardTimeJt());
            }
        }

        if (detail.getProduct() != null) {
            row.setProductId(detail.getProduct().getId());
            row.setProductCode(detail.getProduct().getCode());
            row.setProductName(detail.getProduct().getName());
        }

        if (detail.getTrainingSample() != null) {
            row.setTrainingSampleId(detail.getTrainingSample().getId());
            row.setTrainingSampleName(detail.getTrainingSample().getCategoryName());
        }

        if (detail.getSampleCode() != null) {
            row.setSampleCode(detail.getSampleCode());
        } else if (detail.getTrainingSample() != null
                && detail.getTrainingSample().getTrainingSampleCode() != null) {
            row.setSampleCode(detail.getTrainingSample().getTrainingSampleCode());
        }

        row.setTrainingTopic(detail.getTrainingTopic());

        if (row.getClassification() == null && detail.getClassification() != null) {
            row.setClassification(detail.getClassification());
        }
        if (row.getStandardTime() == null && detail.getCycleTimeStandard() != null) {
            row.setStandardTime(detail.getCycleTimeStandard());
        }

        row.setTimeIn(detail.getTimeIn());
        row.setTimeStartOp(detail.getTimeStartOp());
        row.setTimeOut(detail.getTimeOut());
        row.setDetectionTime(detail.getDetectionTime());

        row.setIsPass(detail.getIsPass());
        row.setIsRetrained(detail.getIsRetrained());
        row.setNote(detail.getNote());

        if (detail.getSignatureProIn() != null) {
            row.setSignatureProInId(detail.getSignatureProIn().getId());
            row.setSignatureProInName(detail.getSignatureProIn().getFullName());
        }
        if (detail.getSignatureFiIn() != null) {
            row.setSignatureFiInId(detail.getSignatureFiIn().getId());
            row.setSignatureFiInName(detail.getSignatureFiIn().getFullName());
        }
        if (detail.getSignatureProOut() != null) {
            row.setSignatureProOutId(detail.getSignatureProOut().getId());
            row.setSignatureProOutName(detail.getSignatureProOut().getFullName());
        }
        if (detail.getSignatureFiOut() != null) {
            row.setSignatureFiOutId(detail.getSignatureFiOut().getId());
            row.setSignatureFiOutName(detail.getSignatureFiOut().getFullName());
        }

        row.setRejectFeedback(detail.getRejectFeedback());

        return row;
    }

//    @Override
//    @Transactional
//    public void submitResult(Long resultId) {
//        TrainingResult result = trainingResultRepository.findByIdWithDetails(resultId)
//                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));
//
//        if (result.getStatus() == ReportStatus.PENDING_REVIEW || result.getStatus() == ReportStatus.PENDING_APPROVAL) {
//            throw new AppException(ErrorCode.INVALID_TRAINING_RESULT_STATUS);
//        }
//
//        boolean allHaveProcess = result.getDetails().stream()
//                .allMatch(d -> d.getProcess() != null);
//        if (!allHaveProcess) {
//            throw new AppException(ErrorCode.MISSING_PROCESS_IN_RESULT_DETAIL);
//        }
//
//        java.time.LocalDate now = java.time.LocalDate.now();
//        for (TrainingResultDetail detail : result.getDetails()) {
//            if (detail.getActualDate() == null) {
//                detail.setActualDate(now);
//            }
//            if (detail.getTrainingPlanDetail() != null && detail.getTrainingPlanDetail().getActualDate() == null) {
//                detail.getTrainingPlanDetail().setActualDate(now);
//            }
//        }
//
//        result.setStatus(ReportStatus.PENDING_REVIEW);
//        trainingResultRepository.save(result);
//    }

    @Override
    public List<TrainingResultOptionResponse> getProcessesByLine(Long lineId) {
        List<com.sep490.anomaly_training_backend.model.Process> processes = processRepository
                .findByProductLineIdAndDeleteFlagFalse(lineId);
        return processes.stream()
                .map(p -> new TrainingResultOptionResponse(p.getId(), p.getCode() + " - " + p.getName()))
                .toList();
    }

    @Override
    public List<TrainingResultProcessResponse> getProcessesByEmployeeSkill(Long employeeId, Long lineId) {
        List<EmployeeSkill> skills = employeeSkillRepository
                .findSkillsByEmployeeAndLine(employeeId, lineId);
        return skills.stream()
                .map(skill -> {
                    com.sep490.anomaly_training_backend.model.Process p = skill.getProcess();
                    return new TrainingResultProcessResponse(p.getId(), p.getCode() + " - " + p.getName(),
                            p.getClassification().getValue());
                })
                .toList();
    }

    @Override
    public List<TrainingResultProductOptionResponse> getProductsByProcess(Long processId) {
        List<ProductProcess> productProcesses = productProcessRepository.findByProcessId(processId);
        return productProcesses.stream()
                .map(pp -> {
                    Product product = pp.getProduct();
                    return new TrainingResultProductOptionResponse(product.getId(),
                            product.getCode() + " - " + product.getName(), pp.getStandardTimeJt());
                })
                .toList();
    }

    @Override
    public List<SampleResultResponse> getSamplesByProduct(Long productId) {
        List<TrainingSample> samples = trainingSampleRepository.findByProductId(productId);
        if (samples.isEmpty()) {
            samples = trainingSampleRepository.findAllSamples();
        }
        return samples.stream()
                .map(sample -> new SampleResultResponse(
                        sample.getId(),
                        sample.getTrainingSampleCode(),
                        sample.getTrainingDescription()))
                .toList();
    }

    @Override
    @Transactional
    public void reviseDetail(Long detailId) {
        TrainingResultDetail detail = trainingResultDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

        // Chỉ cho revise khi detail đang bị reject
        if (detail.getStatus() != ReportStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_TRAINING_RESULT_STATUS);
        }

        // Tạo snapshot detail trước khi thay đổi
        createDetailHistorySnapshot(detail);

        // Chuyển status về PENDING để TL có thể sửa lại
        detail.setStatus(ReportStatus.ONGOING);

        // Xóa kết quả pass/fail vì detail sẽ được nhập lại từ đầu
        detail.setIsPass(null);

        trainingResultDetailRepository.save(detail);
    }

    /**
     * Tạo snapshot cho 1 detail cụ thể (lưu vào training_result_detail_history).
     */
    private void createDetailHistorySnapshot(TrainingResultDetail detail) {
        // Tạo 1 TrainingResultEmployeeHistory header tạm cho snapshot
        TrainingResult result = detail.getTrainingResult();
        TrainingResultHistory history = TrainingResultHistory.builder()
                .trainingResult(result)
                .version(result.getCurrentVersion())
                .title(result.getTitle())
                .year(result.getYear())
                .team_id(result.getTeam() != null ? result.getTeam().getId() : null)
                .lineId(result.getLine() != null ? result.getLine().getId() : null)
                .statusAtTime(result.getStatus() != null ? result.getStatus().name() : null)
                .note("Snapshot khi revise detail #" + detail.getId())
                .detailHistories(new ArrayList<>())
                .build();

        TrainingResultDetailHistory detailHistory = TrainingResultDetailHistory.builder()
                .trainingResultHistory(history)
                .trainingResultDetailId(detail.getId())
                .employeeId(detail.getEmployee() != null ? detail.getEmployee().getId() : null)
                .processId(detail.getProcess() != null ? detail.getProcess().getId() : null)
                .trainingSampleId(detail.getTrainingSample() != null ? detail.getTrainingSample().getId() : null)
                .productId(detail.getProduct() != null ? detail.getProduct().getId() : null)
                .trainingTopic(detail.getTrainingTopic())
                .sampleCode(detail.getSampleCode())
                .classification(detail.getClassification())
                .cycleTimeStandard(detail.getCycleTimeStandard())
                .actualDate(detail.getActualDate())
                .batchId(detail.getBatchId())
                .timeIn(detail.getTimeIn())
                .timeStartOp(detail.getTimeStartOp())
                .timeOut(detail.getTimeOut())
                .detectionTime(detail.getDetectionTime())
                .isPass(detail.getIsPass())
                .signatureProInName(
                        detail.getSignatureProIn() != null ? detail.getSignatureProIn().getFullName() : null)
                .signatureFiInName(detail.getSignatureFiIn() != null ? detail.getSignatureFiIn().getFullName() : null)
                .signatureProOutName(
                        detail.getSignatureProOut() != null ? detail.getSignatureProOut().getFullName() : null)
                .signatureFiOutName(
                        detail.getSignatureFiOut() != null ? detail.getSignatureFiOut().getFullName() : null)
                .build();

        history.getDetailHistories().add(detailHistory);
        trainingResultHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void retrainDetail(Long detailId) {
        TrainingResultDetail originalDetail = trainingResultDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

        originalDetail.setIsRetrained(true);
        originalDetail.setIsPass(false);
        trainingResultDetailRepository.save(originalDetail);

        TrainingResultDetail newDetail = TrainingResultDetail.builder()
                .trainingResult(originalDetail.getTrainingResult())
                .trainingPlanDetail(originalDetail.getTrainingPlanDetail())
                .employee(originalDetail.getEmployee())
                .process(originalDetail.getProcess())
                .product(originalDetail.getProduct())
                .trainingSample(originalDetail.getTrainingSample())
                .classification(originalDetail.getClassification())
                .trainingTopic(originalDetail.getTrainingTopic())
                .sampleCode(originalDetail.getSampleCode())
                .cycleTimeStandard(originalDetail.getCycleTimeStandard())
                .plannedDate(java.time.LocalDate.now())
                .batchId(originalDetail.getBatchId())
                .status(ReportStatus.ONGOING)
                .isRetrained(true)
                .note("[Huấn luyện lại] từ detail #" + detailId)
                .build();

        trainingResultDetailRepository.save(newDetail);
    }

    @Override
    public List<PrioritizedEmployeeResponse> getEmployeesInTeams(Long resultId) {
        TrainingResult result = trainingResultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        Long teamId = result.getTeam() != null ? result.getTeam().getId() : null;
        if (teamId == null)
            return List.of();

        List<Employee> allEmployees = employeeRepository.findAllActiveByTeamId(teamId, EmployeeStatus.ACTIVE);

        Set<Long> inResultIds = new java.util.HashSet<>(
                trainingResultDetailRepository.findEmployeeIdsByTrainingResultId(resultId));

        Map<Long, PrioritySnapshotDetail> snapshotMap = loadSnapshotMap(result.getTrainingPlan().getId());

        Map<Long, TrainingResultDetail> lastTrainingMap = loadLastTrainingMap(
                allEmployees.stream().map(Employee::getId).collect(Collectors.toList()));

        return allEmployees.stream()
                .map(emp -> buildEmployeePlanResponse(emp, snapshotMap, lastTrainingMap, inResultIds))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeSkillCertificateResponse> getSkillCertificates(Long resultId) {
        // 1. Load training result
        TrainingResult result = trainingResultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        Long teamId = result.getTeam() != null ? result.getTeam().getId() : null;
        Long lineId = result.getLine() != null ? result.getLine().getId() : null;
        if (teamId == null || lineId == null)
            return List.of();

        // 2. Lấy tất cả nhân viên active trong team
        List<Employee> employees = employeeRepository.findAllActiveByTeamId(teamId, EmployeeStatus.ACTIVE);
        if (employees.isEmpty())
            return List.of();

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());

        // 3. Batch load toàn bộ dữ liệu — tránh N+1 query
        // 3a. Skills của tất cả nhân viên trong line này
        Map<Long, List<EmployeeSkill>> skillsByEmployee = employeeSkillRepository
                .findByEmployeeIdsAndLineId(employeeIds, lineId)
                .stream()
                .collect(Collectors.groupingBy(es -> es.getEmployee().getId()));

        // 3b. Toàn bộ history training của nhân viên (để tính stats + 6 dot)
        List<TrainingResultDetail> allHistory = trainingResultDetailRepository
                .findAllHistoryByEmployeeIds(employeeIds);

        // Group: employeeId → processId → list history (đã sort DESC actualDate)
        Map<Long, Map<Long, List<TrainingResultDetail>>> historyMap = allHistory.stream()
                .filter(d -> d.getProcess() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getEmployee().getId(),
                        Collectors.groupingBy(d -> d.getProcess().getId())));

        // 3c. Toàn bộ sessions trong kế hoạch này, group theo employeeId
        Map<Long, List<TrainingResultDetail>> sessionsInResult = trainingResultDetailRepository
                .findAllSessionsByResultId(resultId)
                .stream()
                .collect(Collectors.groupingBy(d -> d.getEmployee().getId()));

        // 4. Build response cho từng nhân viên
        return employees.stream()
                .map(emp -> buildCertificateResponse(
                        emp,
                        skillsByEmployee.getOrDefault(emp.getId(), List.of()),
                        historyMap.getOrDefault(emp.getId(), Map.of()),
                        sessionsInResult.getOrDefault(emp.getId(), List.of())))
                .collect(Collectors.toList());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private EmployeeSkillCertificateResponse buildCertificateResponse(
            Employee emp,
            List<EmployeeSkill> skills,
            Map<Long, List<TrainingResultDetail>> historyByProcess,
            List<TrainingResultDetail> sessionsInResult) {

        // Build ProcessCertDetail cho từng chứng chỉ công đoạn
        List<EmployeeSkillCertificateResponse.ProcessCertDetail> processDetails = skills.stream()
                .map(skill -> buildProcessCertDetail(skill, historyByProcess))
                .collect(Collectors.toList());

        // Tính active cert count và rate
        long activeCertCount = skills.stream()
                .filter(es -> es.getStatus() == EmployeeSkillStatus.VALID)
                .filter(es -> es.getExpiryDate() == null || !es.getExpiryDate().isBefore(LocalDate.now()))
                .count();
        double activeRate = skills.isEmpty() ? 0.0
                : Math.round((double) activeCertCount / skills.size() * 1000) / 10.0; // 1 decimal

        // Build sessions trong kế hoạch này
        List<EmployeeSkillCertificateResponse.PlannedSessionDto> plannedSessions = sessionsInResult.stream()
                .map(this::buildPlannedSessionDto)
                .collect(Collectors.toList());

        return EmployeeSkillCertificateResponse.builder()
                .employeeId(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getFullName())
                .activeCertCount((int) activeCertCount)
                .activeRate(activeRate)
                .processDetails(processDetails)
                .plannedSessions(plannedSessions)
                .build();
    }

    private EmployeeSkillCertificateResponse.ProcessCertDetail buildProcessCertDetail(
            EmployeeSkill skill,
            Map<Long, List<TrainingResultDetail>> historyByProcess) {

        Process process = skill.getProcess();
        List<TrainingResultDetail> history = historyByProcess.getOrDefault(
                process.getId(), List.of());

        // Lấy HISTORY_SIZE lần gần nhất (đã sort DESC bởi query)
        List<Boolean> recentHistory = new java.util.ArrayList<>();
        for (int i = 0; i < HISTORY_SIZE; i++) {
            if (i < history.size()) {
                recentHistory.add(history.get(i).getIsPass());
            } else {
                recentHistory.add(null); // không có data → dot xám
            }
        }

        // Stats
        long passCount = history.stream().filter(d -> Boolean.TRUE.equals(d.getIsPass())).count();
        long failCount = history.stream().filter(d -> Boolean.FALSE.equals(d.getIsPass())).count();
        long totalCount = history.stream().filter(d -> d.getIsPass() != null).count();

        // Latest result = phần tử đầu tiên (sort DESC)
        Boolean latestResult = history.isEmpty() ? null : history.get(0).getIsPass();

        // Classification
        Integer classVal = process.getClassification() != null
                ? process.getClassification().getValue()
                : null;

        return EmployeeSkillCertificateResponse.ProcessCertDetail.builder()
                .processId(process.getId())
                .processCode(process.getCode())
                .processName(process.getName())
                .jtCode(process.getStandardTimeJt() != null
                        ? process.getStandardTimeJt().toPlainString()
                        : null)
                .classification(classVal)
                .classificationLabel(classVal != null ? "Loại " + classVal : null)
                .classificationDesc(buildClassificationDesc(classVal))
                .certStatus(skill.getStatus() != null ? skill.getStatus().name() : null)
                .certifiedDate(skill.getCertifiedDate())
                .expiryDate(skill.getExpiryDate())
                .recentHistory(recentHistory)
                .passCount((int) passCount)
                .failCount((int) failCount)
                .totalCount((int) totalCount)
                .latestResult(latestResult)
                .build();
    }

    private EmployeeSkillCertificateResponse.PlannedSessionDto buildPlannedSessionDto(
            TrainingResultDetail detail) {

        // Người xác nhận: ưu tiên FI out → PRO out
        String confirmerName = null;
        if (detail.getSignatureFiOut() != null) {
            confirmerName = detail.getSignatureFiOut().getFullName();
        } else if (detail.getSignatureProOut() != null) {
            confirmerName = detail.getSignatureProOut().getFullName();
        }

        // Đánh giá
        String evaluation;
        if (detail.getIsPass() == null) {
            evaluation = "Dự kiến";
        } else {
            evaluation = Boolean.TRUE.equals(detail.getIsPass()) ? "Đạt" : "Không đạt";
        }

        return EmployeeSkillCertificateResponse.PlannedSessionDto.builder()
                .detailId(detail.getId())
                .plannedDate(detail.getPlannedDate())
                .actualDate(detail.getActualDate())
                .processId(detail.getProcess() != null ? detail.getProcess().getId() : null)
                .processCode(detail.getProcess() != null ? detail.getProcess().getCode() : null)
                .processName(detail.getProcess() != null ? detail.getProcess().getName() : null)
                .sampleCode(detail.getSampleCode())
                .trainingTopic(detail.getTrainingTopic())
                .note(detail.getNote())
                .confirmerName(confirmerName)
                .evaluation(evaluation)
                .isPass(detail.getIsPass())
                .build();
    }

    /**
     * Mô tả phân loại hiển thị trong legend (xem ảnh góc trên phải).
     */
    private String buildClassificationDesc(Integer classVal) {
        if (classVal == null)
            return null;
        return switch (classVal) {
            case 1 -> "CĐ có quá khứ phát sinh phế phẩm";
            case 2 -> "CĐ rank D đảm bảo bởi người thao tác";
            case 3 -> "CĐ có thao tác chỉ định";
            default -> "CĐ khác";
        };
    }

    @Override
    public void submit(Long reportId, User currentUser, HttpServletRequest request) {
        TrainingResult report = getReportById(reportId);

        validateResultForSubmission(report);
        report.setFormCode(
                ReportUtils.generateFormCode(ApprovalEntityType.TRAINING_RESULT, report.getLine().getCode(), reportId));

        approvalService.submit(report, currentUser, request);
        updateResultDetailAfterSubmission(currentUser, report);

        trainingResultRepository.save(report);
    }

    private void validateResultForSubmission(TrainingResult result) {
    }

    private void updateResultDetailAfterSubmission(User currentUser, TrainingResult result) {
        trainingResultDetailRepository.findPendingWithIsPassByResultId(result.getId())
                .forEach(detail -> {
                    detail.setSignatureProIn(currentUser);
                    detail.setSignatureProOut(currentUser);
                    detail.setStatus(ReportStatus.PENDING_CONFIRMATION);
                });
    }

    @Override
    @Transactional
    public void revise(Long reportId, User currentUser, HttpServletRequest request) {
        TrainingResult result = trainingResultRepository.findByIdWithDetails(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        if (result.getStatus() == ReportStatus.REJECTED) {

            throw new AppException(ErrorCode.INVALID_TRAINING_RESULT_STATUS);
        }

        // 1. Tạo snapshot trước khi thay đổi
        createResultHistorySnapshot(result);

        // 2. Chuyển trạng thái result -> REVISE, tăng version
        result.setStatus(ReportStatus.REVISING);
        result.setCurrentVersion(result.getCurrentVersion() + 1);

        // 3. Chuyển các detail bị reject -> PENDING
        for (TrainingResultDetail detail : result.getDetails()) {
            if (detail.getStatus() == ReportStatus.REJECTED) {
                detail.setStatus(ReportStatus.ONGOING);
            }
        }

        trainingResultRepository.save(result);
    }

    @Override
    public void approveDetail(Long reportId, Long detailId, ApproveRequest req, User currentUser,
                              HttpServletRequest request) {
        approve(reportId, currentUser, req, request);
        TrainingResultDetail detail = trainingResultDetailRepository.findById(detailId).get();
        detail.setStatus(ReportStatus.COMPLETED);
        trainingResultDetailRepository.save(detail);
    }

    @Override
    public void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request) {
        TrainingResult report = getReportById(reportId);
        approvalService.approve(report, currentUser, req, request);
        trainingResultRepository.save(report);
    }

    @Override
    public void rejectDetail(Long reportId, Long detailId, RejectRequest req, User currentUser,
                             HttpServletRequest request) {
        reject(reportId, currentUser, req, request);

        TrainingResultDetail detail = trainingResultDetailRepository.findById(detailId).get();
        detail.setStatus(ReportStatus.REJECTED);

        DetailFeedbackRequest detailFeedbackRequest = new DetailFeedbackRequest();
        detailFeedbackRequest.setRejectReasonIds(req.getRejectReasonIds());
        detailFeedbackRequest.setRequiredActionId(req.getRequiredActionId());
        detailFeedbackRequest.setComment(req.getComment());
    }

    @Override
    public void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request) {
        TrainingResult report = getReportById(reportId);
        approvalService.reject(report, currentUser, req, request);
        trainingResultRepository.save(report);
    }

    @Override
    public boolean canApprove(Long reportId, User currentUser) {
        return false;
    }

    @Override
    public void submitConfirmedResult(Long reportId, User currentUser) {
        TrainingResult report = getReportById(reportId);
        List<TrainingResultDetail> details = report.getDetails().stream()
                .filter(d -> d.getStatus() == ReportStatus.PENDING_CONFIRMATION)
                .map(d -> {
                    if (d.getSignatureFiOut() != null && d.getSignatureFiIn() != null &&
                            d.getSignatureFiOut().equals(currentUser) &&
                            d.getSignatureFiIn().equals(currentUser)) {
                        d.setStatus(ReportStatus.PENDING_REVIEW);
                    }
                    return d;
                })
                .toList();
        trainingResultDetailRepository.saveAll(details);
    }

    private TrainingResultDetail getDetailtById(Long id) {
        return trainingResultDetailRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));
    }

    /**
     * Tạo snapshot (history) cho Training Result và tất cả details.
     */
    private void createResultHistorySnapshot(TrainingResult result) {
        TrainingResultHistory history = TrainingResultHistory.builder()
                .trainingResult(result)
                .version(result.getCurrentVersion())
                .title(result.getTitle())
                .year(result.getYear())
                .team_id(result.getTeam() != null ? result.getTeam().getId() : null)
                .lineId(result.getLine() != null ? result.getLine().getId() : null)
                .statusAtTime(result.getStatus() != null ? result.getStatus().name() : null)
                .note(result.getNote())
                .detailHistories(new ArrayList<>())
                .build();

        if (result.getDetails() != null) {
            for (TrainingResultDetail detail : result.getDetails()) {
                TrainingResultDetailHistory detailHistory = TrainingResultDetailHistory.builder()
                        .trainingResultHistory(history)
                        .trainingResultDetailId(detail.getId())
                        .employeeId(detail.getEmployee() != null ? detail.getEmployee().getId() : null)
                        .processId(detail.getProcess() != null ? detail.getProcess().getId() : null)
                        .trainingSampleId(
                                detail.getTrainingSample() != null ? detail.getTrainingSample().getId() : null)
                        .productId(detail.getProduct() != null ? detail.getProduct().getId() : null)
                        .trainingTopic(detail.getTrainingTopic())
                        .sampleCode(detail.getSampleCode())
                        .classification(detail.getClassification())
                        .cycleTimeStandard(detail.getCycleTimeStandard())
                        .actualDate(detail.getActualDate())
                        .batchId(detail.getBatchId())
                        .timeIn(detail.getTimeIn())
                        .timeStartOp(detail.getTimeStartOp())
                        .timeOut(detail.getTimeOut())
                        .detectionTime(detail.getDetectionTime())
                        .isPass(detail.getIsPass())
                        .signatureProInName(
                                detail.getSignatureProIn() != null ? detail.getSignatureProIn().getFullName() : null)
                        .signatureFiInName(
                                detail.getSignatureFiIn() != null ? detail.getSignatureFiIn().getFullName() : null)
                        .signatureProOutName(
                                detail.getSignatureProOut() != null ? detail.getSignatureProOut().getFullName() : null)
                        .signatureFiOutName(
                                detail.getSignatureFiOut() != null ? detail.getSignatureFiOut().getFullName() : null)
                        .build();

                history.getDetailHistories().add(detailHistory);
            }
        }

        trainingResultHistoryRepository.save(history);
    }

    private Map<Long, PrioritySnapshotDetail> loadSnapshotMap(Long planId) {
        return prioritySnapshotRepository.findByTrainingPlanId(planId)
                .map(snapshot -> prioritySnapshotDetailRepository
                        .findBySnapshotId(snapshot.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                d -> d.getEmployee().getId(),
                                d -> d,
                                (d1, d2) -> d1.getTierOrder() <= d2.getTierOrder() ? d1 : d2)))
                .orElse(Map.of());
    }

    private Map<Long, TrainingResultDetail> loadLastTrainingMap(List<Long> employeeIds) {
        if (employeeIds.isEmpty())
            return Map.of();
        return trainingResultDetailRepository
                .findLatestByEmployeeIds(employeeIds)
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getEmployee().getId(),
                        d -> d,
                        (d1, d2) -> d1.getActualDate().isAfter(d2.getActualDate()) ? d1 : d2));
    }

    private PrioritizedEmployeeResponse buildEmployeePlanResponse(
            Employee emp,
            Map<Long, PrioritySnapshotDetail> snapshotMap,
            Map<Long, TrainingResultDetail> lastTrainingMap,
            Set<Long> inPlanIds) {

        PrioritySnapshotDetail snapshot = snapshotMap.get(emp.getId());
        TrainingResultDetail lastTraining = lastTrainingMap.get(emp.getId());

        return PrioritizedEmployeeResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getFullName())
                .status(emp.getStatus())
                .teamId(emp.getTeams() != null ? emp.getTeams().get(0).getId() : null)
                .teamName(emp.getTeams() != null ? emp.getTeams().get(0).getName() : null)
                .groupName(emp.getTeams() != null && emp.getTeams().get(0).getGroup() != null
                        ? emp.getTeams().get(0).getGroup().getName()
                        : null)
                .tierOrder(snapshot != null ? snapshot.getTierOrder() : null)
                .tierName(snapshot != null ? snapshot.getTierName() : null)
                .sortRank(snapshot != null ? snapshot.getSortRank() : null)
                .priorityReason(buildPriorityReason(snapshot))
                .lastTrainedDate(lastTraining != null ? lastTraining.getActualDate() : null)
                .lastTrainedPassed(lastTraining != null ? lastTraining.getIsPass() : null)
                .inCurrentPlan(inPlanIds.contains(emp.getId()))
                .build();
    }

    private String buildPriorityReason(PrioritySnapshotDetail snapshot) {
        if (snapshot == null)
            return null;
        if ("UNTIERED".equals(snapshot.getTierName()))
            return "Không có tiêu chí ưu tiên";
        return snapshot.getTierName() + " — Hạng #" + snapshot.getSortRank();
    }

    private TrainingResult getReportById(Long id) {
        return trainingResultRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));
    }
}
