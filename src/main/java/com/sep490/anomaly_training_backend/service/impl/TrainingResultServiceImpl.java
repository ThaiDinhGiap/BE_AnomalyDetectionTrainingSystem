package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillCertificateResponse;
import com.sep490.anomaly_training_backend.dto.response.KpiSummaryResponse;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.SampleResultResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultProductOptionResponse;
import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
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
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class TrainingResultServiceImpl implements TrainingResultService {

    private final TrainingResultRepository trainingResultRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final UserRepository userRepository;
    private final TrainingResultDetailRepository detailRepository;
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

    private static final int HISTORY_SIZE = 6;

    @Override
    @Transactional
    public void generateTrainingResult(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

        if (!ReportStatus.APPROVED.equals(plan.getStatus())) {
            throw new AppException(ErrorCode.INVALID_TRAINING_PLAN_STATUS);
        }

        TrainingResult result = new TrainingResult();

        result.setTrainingPlan(plan);
        result.setTeam(plan.getTeam());
        result.setLine(plan.getLine());
        result.setYear(plan.getStartDate().getYear());
        result.setTitle("Báo cáo kết quả - " + plan.getTitle());
        result.setStatus(ReportStatus.ON_GOING);
        result.setCurrentVersion(1);

        List<TrainingResultDetail> resultDetails = new ArrayList<>();

        if (plan.getDetails() != null) {
            for (TrainingPlanDetail planDetail : plan.getDetails()) {

                TrainingResultDetail resultDetail = new TrainingResultDetail();
                resultDetail.setTrainingResult(result);
                resultDetail.setTrainingPlanDetail(planDetail);
                resultDetail.setEmployee(planDetail.getEmployee());
                resultDetail.setPlannedDate(planDetail.getPlannedDate());
                resultDetail.setStatus(ReportStatus.PENDING);

                resultDetails.add(resultDetail);
            }
        }

        result.setDetails(resultDetails);
        trainingResultRepository.save(result);
    }

    @Override
    public KpiSummaryResponse getKpiSummary(Long teamId, Long lineId, Integer year) {
        long totalExecuted = detailRepository.countByFilters(teamId, lineId, year);
        long totalPass = detailRepository.countByFiltersAndIsPass(teamId, lineId, year, true);
        long totalFail = detailRepository.countByFiltersAndIsPass(teamId, lineId, year, false);

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
        List<TrainingSample> samples = trainingSampleRepository.findByProcessId(processId);
        return samples.stream()
                .map(s -> new TrainingResultOptionResponse(s.getId(), s.getCategoryName()))
                .toList();
    }

    @Override
    @Transactional
    public void updateResult(UpdateTrainingResultRequest request) {
        if (request == null || request.getId() == null) return;

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TrainingResult header = trainingResultRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        if (request.getTitle() != null) header.setTitle(request.getTitle());
        if (request.getNote() != null) header.setNote(request.getNote());

        trainingResultRepository.save(header);

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<TrainingResultDetail> detailsToSave = new ArrayList<>();

            for (UpdateResultDetailRequest reqDetail : request.getDetails()) {
                TrainingResultDetail detail = detailRepository.findById(reqDetail.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

                if (reqDetail.getProcessId() != null) {
                    com.sep490.anomaly_training_backend.model.Process process =
                            processRepository.findById(reqDetail.getProcessId())
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
                if (reqDetail.getTimeIn() != null) detail.setTimeIn(reqDetail.getTimeIn());
                if (reqDetail.getTimeStartOp() != null) detail.setTimeStartOp(reqDetail.getTimeStartOp());
                if (reqDetail.getTimeOut() != null) detail.setTimeOut(reqDetail.getTimeOut());
                if (reqDetail.getDetectionTime() != null) detail.setDetectionTime(reqDetail.getDetectionTime());

                // Auto-calculate isPass if possible
                if (detail.getTimeIn() != null && detail.getTimeOut() != null && detail.getCycleTimeStandard() != null) {
                    long actualSeconds = java.time.Duration.between(detail.getTimeIn(), detail.getTimeOut()).toSeconds();
                    detail.setIsPass(actualSeconds <= detail.getCycleTimeStandard().longValue());
                }

                // Manual override from FE always takes precedence
                if (reqDetail.getIsPass() != null) {
                    detail.setIsPass(reqDetail.getIsPass());
                }

                if (reqDetail.getNote() != null) detail.setNote(reqDetail.getNote());

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

                if (isFullSigned(detail)) {
                    detail.setStatus(ReportStatus.DONE);
                    if (detail.getTrainingPlanDetail() != null) {
                        detail.getTrainingPlanDetail().setStatus(
                                com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus.DONE);
                    }
                }

                detailsToSave.add(detail);
            }

            detailRepository.saveAll(detailsToSave);
        }
    }

    private boolean isFullSigned(TrainingResultDetail detail) {
        return detail.getSignatureProIn() != null &&
                detail.getSignatureProOut() != null &&
                detail.getSignatureFiIn() != null &&
                detail.getSignatureFiOut() != null;
    }

    @Override
    @Transactional
    public void signDetailsByFi(List<FiSignRequest> requests) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isFiUser = currentUser.hasRole("FINAL_INSPECTION");
        if (!isFiUser) {
            throw new AppException(ErrorCode.FI_PERMISSION_REQUIRED);
        }

        if (requests == null || requests.isEmpty()) return;

        List<TrainingResultDetail> detailsToSave = new ArrayList<>();
        for (FiSignRequest req : requests) {
            TrainingResultDetail detail = detailRepository.findById(req.getId())
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

        detailRepository.saveAll(detailsToSave);
    }

    @Override
    public List<TrainingResultListResponse> getAllTrainingResults(User currentUser, Long lineId) {

        if (currentUser.hasRole("ROLE_FINAL_INSPECTION")) {
            List<Team> teams = teamRepository.findByFinalInspectionId(currentUser.getId());
            if (teams.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> groupIds = teams.stream().map(team -> team.getGroup().getId()).distinct().toList();
            if (lineId != null) {
                return mapToListResponse(
                        trainingResultRepository.findAllByGroupIdsAndLineId(groupIds, lineId));
            } else {
                return mapToListResponse(
                        trainingResultRepository.findAllByGroupIds(groupIds));
            }
        }

        List<ReportStatus> excludedStatuses = Arrays.asList(ReportStatus.DRAFT, ReportStatus.REVISE);

        if (currentUser.hasRole("ROLE_MANAGER")) {
            if (lineId != null) {
                return mapToListResponse(
                        trainingResultRepository.findAllByManagerAndLineId(currentUser.getId(), lineId, excludedStatuses));
            } else {
                return mapToListResponse(
                        trainingResultRepository.findAllByManager(currentUser.getId(), excludedStatuses));
            }
        }

        if (currentUser.hasRole("ROLE_SUPERVISOR")) {
            List<com.sep490.anomaly_training_backend.model.Group> groups = groupRepository.findBySupervisorId(currentUser.getId());
            if (groups.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> groupIds = groups.stream().map(com.sep490.anomaly_training_backend.model.Group::getId).distinct().toList();

            if (lineId != null) {
                return mapToListResponse(
                        trainingResultRepository.findAllByGroupIdsAndLineId(groupIds, lineId));
            } else {
                return mapToListResponse(
                        trainingResultRepository.findAllByGroupIds(groupIds));
            }
        }

        if (lineId != null) {
            return mapToListResponse(
                    trainingResultRepository.findAllByCreatedByAndLineId(currentUser.getUsername(), lineId));
        } else {
            return mapToListResponse(
                    trainingResultRepository.findAllByCreatedBy(currentUser.getUsername()));
        }
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
            dto.setApprovedAt(entity.getUpdatedAt());

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

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public TrainingResultDetailResponse getTrainingResultDetail(Long id) {
        TrainingResult result = trainingResultRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        TrainingResultDetailResponse response = new TrainingResultDetailResponse();

        response.setId(result.getId());
        response.setTitle(result.getTitle());
        response.setStatus(result.getStatus() != null ? result.getStatus().toString() : null);
        response.setCurrentVersion(result.getCurrentVersion());
        response.setNote(result.getNote());
        response.setYear(result.getYear());
        response.setCreatedAt(result.getCreatedAt());
        response.setCreatedByName(result.getCreatedBy());

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

        List<TrainingResultDetailResponse.DetailRowDto> detailDtos = new ArrayList<>();

        for (TrainingResultDetail detail : result.getDetails()) {
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
                    row.setClassification(String.valueOf(detail.getClassification()));
                } else if (detail.getProcess().getClassification() != null) {
                    row.setClassification("C" + detail.getProcess().getClassification().getValue());
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
            } else if (detail.getTrainingSample() != null && detail.getTrainingSample().getTrainingSampleCode() != null) {
                row.setSampleCode(detail.getTrainingSample().getTrainingSampleCode());
            }

            row.setTrainingTopic(detail.getTrainingTopic());

            if (row.getClassification() == null && detail.getClassification() != null) {
                row.setClassification(String.valueOf(detail.getClassification()));
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

            detailDtos.add(row);
        }

        response.setDetails(detailDtos);
        return response;
    }

    @Override
    @Transactional
    public void submitResult(Long resultId) {
        TrainingResult result = trainingResultRepository.findByIdWithDetails(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        if (result.getStatus() == ReportStatus.WAITING_SV || result.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new AppException(ErrorCode.INVALID_TRAINING_RESULT_STATUS);
        }

        boolean allHaveProcess = result.getDetails().stream()
                .allMatch(d -> d.getProcess() != null);
        if (!allHaveProcess) {
            throw new AppException(ErrorCode.MISSING_PROCESS_IN_RESULT_DETAIL);
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        for (TrainingResultDetail detail : result.getDetails()) {
            if (detail.getActualDate() == null) {
                detail.setActualDate(now);
            }
            if (detail.getTrainingPlanDetail() != null && detail.getTrainingPlanDetail().getActualDate() == null) {
                detail.getTrainingPlanDetail().setActualDate(now);
            }
        }

        result.setStatus(ReportStatus.WAITING_SV);
        trainingResultRepository.save(result);
    }

    @Override
    public List<TrainingResultOptionResponse> getProcessesByLine(Long lineId) {
        List<com.sep490.anomaly_training_backend.model.Process> processes =
                processRepository.findByProductLineIdAndDeleteFlagFalse(lineId);
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
                    return new TrainingResultProcessResponse(p.getId(), p.getCode() + " - " + p.getName(), p.getClassification());
                })
                .toList();
    }

    @Override
    public List<TrainingResultProductOptionResponse> getProductsByProcess(Long processId) {
        List<ProductProcess> productProcesses = productProcessRepository.findByProcessId(processId);
        return productProcesses.stream()
                .map(pp -> {
                    Product product = pp.getProduct();
                    return new TrainingResultProductOptionResponse(product.getId(), product.getCode() + " - " + product.getName(), pp.getStandardTimeJt());
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
                        sample.getTrainingDescription()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void rejectDetail(Long detailId, String reason) {
        TrainingResultDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

        detail.setIsPass(false);
        detail.setStatus(ReportStatus.REJECTED_BY_SV);

        if (reason != null && !reason.isBlank()) {
            String existingNote = detail.getNote() != null ? detail.getNote() : "";
            detail.setNote(existingNote + (existingNote.isEmpty() ? "" : " | ") + "[Từ chối] " + reason);
        }

        detailRepository.save(detail);
    }

    @Override
    @Transactional
    public void retrainDetail(Long detailId) {
        TrainingResultDetail originalDetail = detailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));

        originalDetail.setIsRetrained(true);
        originalDetail.setIsPass(false);
        detailRepository.save(originalDetail);

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
                .status(ReportStatus.PENDING)
                .isRetrained(true)
                .note("[Huấn luyện lại] từ detail #" + detailId)
                .build();

        detailRepository.save(newDetail);
    }

    @Override
    public List<PrioritizedEmployeeResponse> getEmployeesInTeams(Long resultId) {
        TrainingResult result = trainingResultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_NOT_FOUND));

        Long teamId = result.getTeam() != null ? result.getTeam().getId() : null;
        if (teamId == null) return List.of();

        List<Employee> allEmployees = employeeRepository.findAllActiveByTeamId(teamId);

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
        if (teamId == null || lineId == null) return List.of();

        // 2. Lấy tất cả nhân viên active trong team
        List<Employee> employees = employeeRepository.findAllActiveByTeamId(teamId);
        if (employees.isEmpty()) return List.of();

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
                        Collectors.groupingBy(d -> d.getProcess().getId())
                ));

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
                        sessionsInResult.getOrDefault(emp.getId(), List.of())
                ))
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
                ? process.getClassification().getValue() : null;

        return EmployeeSkillCertificateResponse.ProcessCertDetail.builder()
                .processId(process.getId())
                .processCode(process.getCode())
                .processName(process.getName())
                .jtCode(process.getStandardTimeJt() != null
                        ? process.getStandardTimeJt().toPlainString() : null)
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
        if (classVal == null) return null;
        return switch (classVal) {
            case 1 -> "CĐ có quá khứ phát sinh phế phẩm";
            case 2 -> "CĐ rank D đảm bảo bởi người thao tác";
            case 3 -> "CĐ có thao tác chỉ định";
            default -> "CĐ khác";
        };
    }

    @Override
    public void submitDetailForApproval(Long resultId, User currentUser, HttpServletRequest request) {
        TrainingResultDetail detail = getDetailtById(resultId);
    }

    @Override
    public void submit(Long reportId, User currentUser, HttpServletRequest request) {
    }

    @Override
    public void revise(Long reportId, User currentUser, HttpServletRequest request) {
    }

    @Override
    public void approve(Long reportId, User currentUser, ApproveRequest req, HttpServletRequest request) {
    }

    @Override
    public void reject(Long reportId, User currentUser, RejectRequest req, HttpServletRequest request) {
    }


    @Override
    public boolean canApprove(Long reportId, User currentUser) {
        return false;
    }

    private TrainingResultDetail getDetailtById(Long id) {
        return detailRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_RESULT_DETAIL_NOT_FOUND));
    }

    private Map<Long, PrioritySnapshotDetail> loadSnapshotMap(Long planId) {
        return prioritySnapshotRepository.findByTrainingPlanId(planId)
                .map(snapshot -> prioritySnapshotDetailRepository
                        .findBySnapshotId(snapshot.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                d -> d.getEmployee().getId(),
                                d -> d,
                                (d1, d2) -> d1.getTierOrder() <= d2.getTierOrder() ? d1 : d2
                        )))
                .orElse(Map.of());
    }

    private Map<Long, TrainingResultDetail> loadLastTrainingMap(List<Long> employeeIds) {
        if (employeeIds.isEmpty()) return Map.of();
        return trainingResultDetailRepository
                .findLatestByEmployeeIds(employeeIds)
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getEmployee().getId(),
                        d -> d,
                        (d1, d2) -> d1.getActualDate().isAfter(d2.getActualDate()) ? d1 : d2
                ));
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
                .teamId(emp.getTeam() != null ? emp.getTeam().getId() : null)
                .teamName(emp.getTeam() != null ? emp.getTeam().getName() : null)
                .groupName(emp.getTeam() != null && emp.getTeam().getGroup() != null
                        ? emp.getTeam().getGroup().getName() : null)
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
        if (snapshot == null) return null;
        if ("UNTIERED".equals(snapshot.getTierName())) return "Không có tiêu chí ưu tiên";
        return snapshot.getTierName() + " — Hạng #" + snapshot.getSortRank();
    }
}
