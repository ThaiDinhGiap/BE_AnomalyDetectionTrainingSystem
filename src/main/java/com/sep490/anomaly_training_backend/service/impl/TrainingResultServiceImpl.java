package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.ResourceNotFoundException;
import com.sep490.anomaly_training_backend.model.Product;
import com.sep490.anomaly_training_backend.model.TrainingSample;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Override
    @Transactional
    public void generateTrainingResult(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Kế hoạch ID: " + planId));

        if (!ReportStatus.APPROVED.equals(plan.getStatus())) {
            throw new IllegalStateException("Chỉ được tạo kết quả cho các kế hoạch ĐÃ DUYỆT (APPROVED).");
        }

        TrainingResult result = new TrainingResult();

        result.setTrainingPlan(plan);
        result.setTeam(plan.getTeam());
        result.setLine(plan.getLine());
        result.setYear(plan.getMonthStart().getYear());
        result.setTitle("Báo cáo kết quả - " + plan.getTitle());
        result.setStatus(ReportStatus.ON_GOING);
        result.setCurrentVersion(1);

        List<TrainingResultDetail> resultDetails = new ArrayList<>();

        if (plan.getDetails() != null) {
            for (TrainingPlanDetail planDetail : plan.getDetails()) {

                TrainingResultDetail resultDetail = new TrainingResultDetail();
                resultDetail.setTrainingResult(result);
                resultDetail.setTrainingPlanDetail(planDetail);

                // Copy employee từ plan detail
                resultDetail.setEmployee(planDetail.getEmployee());

                // Process: tạm để null, TL sẽ chọn khi update result
                // (vì plan detail không có process - process nằm ở employee_skill)

                resultDetail.setPlannedDate(planDetail.getPlannedDate());
                resultDetail.setStatus(ReportStatus.PENDING);

                resultDetails.add(resultDetail);
            }
        }

        result.setDetails(resultDetails);
        trainingResultRepository.save(result);
    }

    @Override
    public List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId) {
        // Trả về danh sách Product (mã sản phẩm) - hiện tại lấy tất cả
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(p -> new TrainingResultOptionResponse(p.getId(), p.getCode() + " - " + p.getName()))
                .toList();
    }

    // 2. Lấy Hạng mục huấn luyện bất thường theo Công đoạn
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

        // 1. Lấy User hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // =================================================================
        // PHẦN 1: XỬ LÝ HEADER (TrainingResult)
        // =================================================================
        TrainingResult header = trainingResultRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("TrainingResult", "id", request.getId()));

        if (request.getTitle() != null) header.setTitle(request.getTitle());
        if (request.getNote() != null) header.setNote(request.getNote());

        trainingResultRepository.save(header);

        // =================================================================
        // PHẦN 2: XỬ LÝ DETAILS
        // =================================================================
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<TrainingResultDetail> detailsToSave = new ArrayList<>();

            for (UpdateResultDetailRequest reqDetail : request.getDetails()) {
                TrainingResultDetail detail = detailRepository.findById(reqDetail.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "TrainingResultDetail", "id", reqDetail.getId()));

                // A. Chọn Công đoạn (Process dropdown)
                if (reqDetail.getProcessId() != null) {
                    com.sep490.anomaly_training_backend.model.Process process =
                            processRepository.findById(reqDetail.getProcessId())
                                    .orElseThrow(() -> new EntityNotFoundException(
                                            "Công đoạn ID " + reqDetail.getProcessId() + " không tồn tại"));
                    detail.setProcess(process);

                    // Auto-fill classification & standardTime từ process (nếu user không gửi)
                    if (reqDetail.getClassification() == null && process.getClassification() != null) {
                        detail.setClassification(process.getClassification().getValue());
                    }
                    if (reqDetail.getCycleTimeStandard() == null && process.getStandardTimeJt() != null) {
                        detail.setCycleTimeStandard(process.getStandardTimeJt());
                    }
                }

                // B. Chọn Mã sản phẩm (Product)
                if (reqDetail.getProductId() != null) {
                    Product product = productRepository.findById(reqDetail.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Sản phẩm ID " + reqDetail.getProductId() + " không tồn tại"));
                    detail.setProduct(product);
                }

                // C. Chọn Hạng mục huấn luyện bất thường (Training Sample dropdown)
                if (reqDetail.getTrainingSampleId() != null) {
                    TrainingSample sample = trainingSampleRepository.findById(reqDetail.getTrainingSampleId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Hạng mục huấn luyện ID " + reqDetail.getTrainingSampleId() + " không tồn tại"));
                    detail.setTrainingSample(sample);
                    // Auto-fill sampleCode từ sample nếu user không gửi
                    if (reqDetail.getSampleCode() == null && sample.getSampleCode() != null) {
                        detail.setSampleCode(sample.getSampleCode());
                    }
                }

                // D. Override classification & cycleTimeStandard nếu user gửi lên
                if (reqDetail.getClassification() != null) {
                    detail.setClassification(reqDetail.getClassification());
                }
                if (reqDetail.getCycleTimeStandard() != null) {
                    detail.setCycleTimeStandard(reqDetail.getCycleTimeStandard());
                }

                // E. Số quản lý mẫu
                if (reqDetail.getSampleCode() != null) {
                    detail.setSampleCode(reqDetail.getSampleCode());
                }

                // F. Training topic (text tự do)
                if (reqDetail.getTrainingTopic() != null) {
                    detail.setTrainingTopic(reqDetail.getTrainingTopic());
                }

                // G. Ngày thực tế
                if (reqDetail.getActualDate() != null) {
                    detail.setActualDate(reqDetail.getActualDate());
                }

                // H. Thời gian: đưa mẫu vào, bắt đầu vòng thao tác, lấy mẫu ra
                if (reqDetail.getTimeIn() != null) detail.setTimeIn(reqDetail.getTimeIn());
                if (reqDetail.getTimeStartOp() != null) detail.setTimeStartOp(reqDetail.getTimeStartOp());
                if (reqDetail.getTimeOut() != null) detail.setTimeOut(reqDetail.getTimeOut());

                // I. Thời gian phát hiện
                if (reqDetail.getDetectionTime() != null) detail.setDetectionTime(reqDetail.getDetectionTime());

                // J. Đánh giá Đạt/Trượt (auto hoặc manual)
                // Auto: nếu có timeIn, timeOut và cycleTimeStandard → so sánh
                if (reqDetail.getIsPass() != null) {
                    // Manual override
                    detail.setIsPass(reqDetail.getIsPass());
                } else if (detail.getTimeIn() != null && detail.getTimeOut() != null
                        && detail.getCycleTimeStandard() != null) {
                    // Auto calculate
                    long actualSeconds = java.time.Duration.between(detail.getTimeIn(), detail.getTimeOut()).toSeconds();
                    boolean autoPass = actualSeconds <= detail.getCycleTimeStandard().longValue();
                    detail.setIsPass(autoPass);
                }

                // K. Ghi chú
                if (reqDetail.getNote() != null) detail.setNote(reqDetail.getNote());

                // L. Chữ ký Pro (vào/ra)
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

                // M. Huấn luyện lại
                if (reqDetail.getIsRetrained() != null) {
                    detail.setIsRetrained(reqDetail.getIsRetrained());
                }

                // N. Auto set actualDate khi đủ 4 chữ ký
                if (isFullSigned(detail)) {
                    if (detail.getActualDate() == null) {
                        detail.setActualDate(java.time.LocalDate.now());
                    }
                    detail.setStatus(ReportStatus.DONE);
                    // Update plan detail
                    if (detail.getTrainingPlanDetail() != null) {
                        detail.getTrainingPlanDetail().setActualDate(detail.getActualDate());
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
        // 1. Kiểm tra quyền hạn: Bắt buộc phải là FI
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        String userRole = currentUser.getRole().toString();
        boolean isFiUser = "FINAL_INSPECTION".equalsIgnoreCase(userRole);

        if (!isFiUser) {
            throw new RuntimeException("Truy cập bị từ chối: Chỉ quản lý (FI) mới được quyền ký duyệt.");
        }

        if (requests == null || requests.isEmpty()) return;

        List<TrainingResultDetail> detailsToSave = new ArrayList<>();

        for (FiSignRequest req : requests) {
            TrainingResultDetail detail = detailRepository.findById(req.getId())
                    .orElseThrow(() -> new RuntimeException("Detail not found ID: " + req.getId()));


            if (Boolean.TRUE.equals(req.getIsSignIn())) {
                if (detail.getSignatureFiIn() == null) {
                    detail.setSignatureFiIn(currentUser);
                }
            }

            // --- Ký Đầu Ra ---
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
    public List<TrainingResultListResponse> getAllTrainingResults() {
        List<TrainingResult> entities = trainingResultRepository.findByDeleteFlagFalse();
        return mapToListResponse(entities);
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

            // Lấy monthList từ plan monthStart/monthEnd
            TrainingPlan plan = entity.getTrainingPlan();
            if (plan != null && plan.getMonthStart() != null && plan.getMonthEnd() != null) {
                List<String> months = new ArrayList<>();
                java.time.LocalDate cursor = plan.getMonthStart().withDayOfMonth(1);
                java.time.LocalDate end = plan.getMonthEnd().withDayOfMonth(1);
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
                .orElseThrow(() -> new ResourceNotFoundException("TrainingResult", "id", id));

        TrainingResultDetailResponse response = new TrainingResultDetailResponse();

        // 1. Map Header
        response.setId(result.getId());
        response.setTitle(result.getTitle());
        response.setStatus(result.getStatus() != null ? result.getStatus().toString() : null);
        response.setCurrentVersion(result.getCurrentVersion());
        response.setNote(result.getNote());
        response.setYear(result.getYear());
        response.setCreatedAt(result.getCreatedAt());
        response.setCreatedByName(result.getCreatedBy());

        // 2. Line / Group info
        if (result.getLine() != null) {
            response.setLineId(result.getLine().getId());
            response.setLineName(result.getLine().getName());
            if (result.getLine().getGroup() != null) {
                response.setGroupId(result.getLine().getGroup().getId());
                response.setGroupName(result.getLine().getGroup().getName());
            }
        }

        // 3. Plan info
        if (result.getTrainingPlan() != null) {
            response.setTrainingPlanId(result.getTrainingPlan().getId());
            response.setTrainingPlanTitle(result.getTrainingPlan().getTitle());
        }

        // 4. Map Details
        List<TrainingResultDetailResponse.DetailRowDto> detailDtos = new ArrayList<>();

        for (TrainingResultDetail detail : result.getDetails()) {
            TrainingResultDetailResponse.DetailRowDto row = new TrainingResultDetailResponse.DetailRowDto();

            row.setId(detail.getId());
            row.setPlannedDate(detail.getPlannedDate());
            row.setActualDate(detail.getActualDate());
            row.setDetailStatus(detail.getStatus() != null ? detail.getStatus().toString() : null);

            // Plan detail info
            TrainingPlanDetail planDetail = detail.getTrainingPlanDetail();
            if (planDetail != null) {
                row.setTrainingPlanDetailId(planDetail.getId());
                row.setBatchId(planDetail.getBatchId());

                // Employee info from plan detail
                if (planDetail.getEmployee() != null) {
                    row.setEmployeeId(planDetail.getEmployee().getId());
                    row.setEmployeeName(planDetail.getEmployee().getFullName());
                    row.setEmployeeCode(planDetail.getEmployee().getEmployeeCode());
                }
            }

            // Employee info trực tiếp từ result detail (ưu tiên nếu có)
            if (detail.getEmployee() != null) {
                row.setEmployeeId(detail.getEmployee().getId());
                row.setEmployeeName(detail.getEmployee().getFullName());
                row.setEmployeeCode(detail.getEmployee().getEmployeeCode());
            }

            // Process info
            if (detail.getProcess() != null) {
                row.setProcessId(detail.getProcess().getId());
                row.setProcessCode(detail.getProcess().getCode());
                row.setProcessName(detail.getProcess().getName());
                // Classification từ process (format: "MS.1.2" kiểu string)
                if (detail.getClassification() != null) {
                    row.setClassification(String.valueOf(detail.getClassification()));
                } else if (detail.getProcess().getClassification() != null) {
                    row.setClassification("C" + detail.getProcess().getClassification().getValue());
                }
                // Standard time từ process
                if (detail.getCycleTimeStandard() != null) {
                    row.setStandardTime(detail.getCycleTimeStandard());
                } else if (detail.getProcess().getStandardTimeJt() != null) {
                    row.setStandardTime(detail.getProcess().getStandardTimeJt());
                }
            }

            // Product info (Mã sản phẩm)
            if (detail.getProduct() != null) {
                row.setProductId(detail.getProduct().getId());
                row.setProductCode(detail.getProduct().getCode());
                row.setProductName(detail.getProduct().getName());
            }

            // Training Sample info (Hạng mục huấn luyện bất thường)
            if (detail.getTrainingSample() != null) {
                row.setTrainingSampleId(detail.getTrainingSample().getId());
                row.setTrainingSampleName(detail.getTrainingSample().getCategoryName());
            }

            // Số quản lý mẫu
            if (detail.getSampleCode() != null) {
                row.setSampleCode(detail.getSampleCode());
            } else if (detail.getTrainingSample() != null && detail.getTrainingSample().getSampleCode() != null) {
                row.setSampleCode(detail.getTrainingSample().getSampleCode());
            }

            // Training topic (text tự do - nếu không chọn sample)
            row.setTrainingTopic(detail.getTrainingTopic());

            // Classification & standardTime fallback (nếu chưa set ở trên)
            if (row.getClassification() == null && detail.getClassification() != null) {
                row.setClassification(String.valueOf(detail.getClassification()));
            }
            if (row.getStandardTime() == null && detail.getCycleTimeStandard() != null) {
                row.setStandardTime(detail.getCycleTimeStandard());
            }

            // Time tracking
            row.setTimeIn(detail.getTimeIn());
            row.setTimeStartOp(detail.getTimeStartOp());
            row.setTimeOut(detail.getTimeOut());
            row.setDetectionTime(detail.getDetectionTime());

            // Result
            row.setIsPass(detail.getIsPass());
            row.setIsRetrained(detail.getIsRetrained());
            row.setNote(detail.getNote());

            // Signatures
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
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kết quả ID: " + resultId));

        if (result.getStatus() == ReportStatus.WAITING_SV || result.getStatus() == ReportStatus.WAITING_MANAGER) {
            throw new IllegalStateException("Kết quả này đã được gửi duyệt trước đó.");
        }

        // Kiểm tra tất cả detail đã có process được chọn
        boolean allHaveProcess = result.getDetails().stream()
                .allMatch(d -> d.getProcess() != null);
        if (!allHaveProcess) {
            throw new IllegalArgumentException("Không thể gửi. Vui lòng chọn Công đoạn cho tất cả các hạng mục.");
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
    @Transactional
    public void rejectDetail(Long detailId, String reason) {
        TrainingResultDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingResultDetail", "id", detailId));

        // Đánh dấu trượt
        detail.setIsPass(false);
        detail.setStatus(ReportStatus.REJECTED_BY_SV);

        // Ghi lý do vào note
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
                .orElseThrow(() -> new ResourceNotFoundException("TrainingResultDetail", "id", detailId));

        // Đánh dấu detail gốc là đã retrain
        originalDetail.setIsRetrained(true);
        originalDetail.setIsPass(false);
        detailRepository.save(originalDetail);

        // Tạo detail mới (clone) cho lần huấn luyện lại
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
                .plannedDate(java.time.LocalDate.now()) // Ngày mới
                .status(ReportStatus.PENDING)
                .isRetrained(true)
                .note("[Huấn luyện lại] từ detail #" + detailId)
                .build();

        detailRepository.save(newDetail);
    }

    // Relate approval methods
    @Override
    public void submitDetailForApproval(Long resultId, User currentUser, HttpServletRequest request) {
        TrainingResultDetail detail = getDetailtById(resultId);
//        approvalService.submit(detail, currentUser, request);
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

    // private methods
    private TrainingResultDetail getDetailtById(Long id) {
        return detailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingResultDetail", "id", id));
    }
}
