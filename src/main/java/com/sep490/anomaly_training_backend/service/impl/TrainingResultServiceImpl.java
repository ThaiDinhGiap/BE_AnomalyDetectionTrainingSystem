package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.FiSignRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateResultDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.UpdateTrainingResultRequest;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultListResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingResultOptionResponse;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.enums.TrainingResultDetailStatus;
import com.sep490.anomaly_training_backend.model.GroupProduct;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.TrainingTopic;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupProductRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.TrainingTopicRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final GroupProductRepository groupProductRepository;
    private final TrainingTopicRepository trainingTopicRepository;
    private final UserRepository userRepository;
    private final TrainingResultDetailRepository detailRepository;

    @Override
    @Transactional
    public void generateTrainingResult(Long planId) {
        TrainingPlan plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Kế hoạch ID: " + planId));

        if (!ReportStatus.APPROVED.equals(plan.getStatus())) {
            throw new IllegalStateException("Chỉ được tạo kết quả cho các kế hoạch ĐÃ DUYỆT (APPROVED).");
        }

        TrainingResult result = new TrainingResult();

        result.setGroup(plan.getGroup());
        result.setYear(plan.getMonthStart().getYear());
        result.setTitle("Báo cáo kết quả - " + plan.getTitle());
        result.setStatus(ReportStatus.ON_GOING);
        result.setCurrentVersion(1);

        String groupCode = plan.getGroup().getName().toUpperCase().replaceAll("\\s+", "");
        result.setFormCode("TR_RESULT_" + groupCode + "_" + result.getYear());

        List<TrainingResultDetail> resultDetails = new ArrayList<>();

        if (plan.getDetails() != null) {
            for (TrainingPlanDetail planDetail : plan.getDetails()) {

                TrainingResultDetail resultDetail = new TrainingResultDetail();

                resultDetail.setTrainingResult(result);

                resultDetail.setTrainingPlanDetail(planDetail);

                resultDetail.setPlannedDate(planDetail.getPlannedDate());

                resultDetail.setStatus(TrainingResultDetailStatus.PENDING);

                resultDetails.add(resultDetail);
            }
        }

        result.setDetails(resultDetails);
        trainingResultRepository.save(result);
    }

    @Override
    public List<TrainingResultOptionResponse> getProductGroupsByLine(Long groupId) {
        List<GroupProduct> list = groupProductRepository.findByGroupId(groupId);

        return list.stream()
                .map(item -> new TrainingResultOptionResponse(
                        item.getId(),
                        item.getProductCode()
                ))
                .toList();
    }

    // 2. Lấy Training Topic theo Công đoạn
    @Override
    public List<TrainingResultOptionResponse> getTrainingTopicsByProcess(Long processId) {
        List<TrainingTopic> list = trainingTopicRepository.findByProcessId(processId);

        return list.stream()
                .map(item -> new TrainingResultOptionResponse(
                        item.getId(),
                        item.getTrainingSample()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void updateResult(UpdateTrainingResultRequest request) {
        if (request == null) return;

        // 1. Lấy User hiện tại
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String userRole = currentUser.getRole().toString();
        boolean isFiUser = "FINAL_INSPECTION".equalsIgnoreCase(userRole);
        boolean isProUser = "TEAM_LEADER".equalsIgnoreCase(userRole);

        // =================================================================
        // PHẦN 1: XỬ LÝ HEADER (TrainingResult)
        // =================================================================
        TrainingResult header = trainingResultRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Header not found ID: " + request.getId()));

        if (isProUser) {
            if (request.getTitle() != null) header.setTitle(request.getTitle());
            if (request.getNote() != null) header.setNote(request.getNote());
        }

        if (request.getStatus() != null) {
            header.setStatus(request.getStatus());
        }

        trainingResultRepository.save(header);

        // =================================================================
        // PHẦN 2: XỬ LÝ DETAILS
        // =================================================================
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<TrainingResultDetail> detailsToSave = new ArrayList<>();

            for (UpdateResultDetailRequest reqDetail : request.getDetails()) {
                TrainingResultDetail detail = detailRepository.findById(reqDetail.getId())
                        .orElseThrow(() -> new RuntimeException("Detail not found ID: " + reqDetail.getId()));

                // A. Cập nhật ngày thực tế
                if (detail.getActualDate() == null) {
                    detail.setActualDate(LocalDate.now());
                }

                // B. Cập nhật thông tin cấu hình (Topic, Group...) trước để lấy Process chuẩn
                if (isProUser) {
                    if (reqDetail.getProductGroupId() != null) {
                        detail.setProductGroup(groupProductRepository.findById(reqDetail.getProductGroupId()).orElse(null));
                    }
                    // Update Topic
                    if (reqDetail.getTrainingTopicId() != null) {
                        detail.setTrainingTopic(trainingTopicRepository.findById(reqDetail.getTrainingTopicId()).orElseThrow());
                        detail.setTrainingSample(null);
                    } else if (reqDetail.getTrainingSample() != null && !reqDetail.getTrainingSample().isBlank()) {
                        detail.setTrainingTopic(null);
                        detail.setTrainingSample(reqDetail.getTrainingSample());
                    }

                    // Update text fields
                    if (reqDetail.getDetectionTime() != null) detail.setDetectionTime(reqDetail.getDetectionTime());
                    if (reqDetail.getRemedialAction() != null) detail.setRemedialAction(reqDetail.getRemedialAction());
                    if (reqDetail.getNote() != null) detail.setNote(reqDetail.getNote());
                }

                // C. Cập nhật Giờ (TimeIn/TimeOut)
                if (reqDetail.getTimeIn() != null) detail.setTimeIn(reqDetail.getTimeIn());
                if (reqDetail.getTimeOut() != null) detail.setTimeOut(reqDetail.getTimeOut());

                // =================================================================
                // D. LOGIC TỰ ĐỘNG TÍNH PASS/FAIL
                // =================================================================
                if (detail.getTimeIn() != null && detail.getTimeOut() != null) {
                    // Chỉ tính toán nếu detail có liên kết với TrainingTopic -> Process
                    if (detail.getTrainingTopic() != null && detail.getTrainingTopic().getProcess() != null) {
                        Process process = detail.getTrainingTopic().getProcess();

                        // Kiểm tra xem Process có set thời gian chuẩn không
                        if (process.getStandardTimeJt() != null) {
                            // 1. Tính thời gian thực tế (Giây)
                            long actualSeconds = java.time.Duration.between(detail.getTimeIn(), detail.getTimeOut()).toSeconds();

                            // 2. Lấy thời gian chuẩn
                            double standardSeconds = process.getStandardTimeJt().doubleValue();

                            // 3. So sánh: Thực tế <= Chuẩn => PASS
                            boolean autoPass = actualSeconds <= standardSeconds;
                            detail.setIsPass(autoPass);
                        }
                    }
                }

                // E. Ghi đè thủ công (Nếu User gửi isPass lên thì ưu tiên lấy của User)
                if (isProUser && reqDetail.getIsPass() != null) {
                    detail.setIsPass(reqDetail.getIsPass());
                }

                // F. Xử lý ký (Signature)
                if (Boolean.TRUE.equals(reqDetail.getIsSignIn())) {
                    if (isProUser) detail.setSignatureProIn(currentUser.getId());
                    else if (isFiUser) detail.setSignatureFiIn(currentUser);
                }

                if (Boolean.TRUE.equals(reqDetail.getIsSignOut())) {
                    if (isProUser) detail.setSignatureProOut(currentUser.getId());
                    else if (isFiUser) detail.setSignatureFiOut(currentUser);
                }

                detail.setStatus(TrainingResultDetailStatus.PENDING);
                detailsToSave.add(detail);

                if (isFullSigned(detail)) {
                    LocalDate today = LocalDate.now();
                    detail.setActualDate(today);
                    if (detail.getTrainingPlanDetail() != null) {
                        detail.getTrainingPlanDetail().setActualDate(today);
                        detail.getTrainingPlanDetail().setStatus(TrainingPlanDetailStatus.DONE);
                    }
                }
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

        List<TrainingResult> entities = trainingResultRepository.findAll();

        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        return entities.stream().map(entity -> {
            TrainingResultListResponse dto = new TrainingResultListResponse();
            dto.setId(entity.getId());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setApprovedAt(entity.getUpdatedAt());

            if (entity.getStatus() != null) {
                dto.setStatus(entity.getStatus().toString());
            }

            if (entity.getCreatedBy() != null) {
                dto.setCreatedBy(entity.getCreatedBy());
            }

            if (entity.getDetails() != null && !entity.getDetails().isEmpty()) {
                String monthListStr = entity.getDetails().stream()
                        .map(TrainingResultDetail::getActualDate)
                        .filter(Objects::nonNull)
                        .map(date -> date.format(monthYearFormatter))
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(", "));

                dto.setMonthList(monthListStr);
            } else {
                dto.setMonthList("");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public TrainingResultDetailResponse getTrainingResultDetail(Long id) {
        TrainingResult result = trainingResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả ID: " + id));

        TrainingResultDetailResponse response = new TrainingResultDetailResponse();

        // 1. Map Header
        response.setId(result.getId());
        if (result.getGroup() != null) response.setLineName(result.getGroup().getName());
        if (result.getCreatedBy() != null) response.setCreatedByName(result.getCreatedBy());

        List<TrainingResultDetailResponse.DetailRowDto> detailDtos = new ArrayList<>();

        for (TrainingResultDetail detail : result.getDetails()) {
            TrainingResultDetailResponse.DetailRowDto row = new TrainingResultDetailResponse.DetailRowDto();

            row.setId(detail.getId());
            row.setActualDate(detail.getActualDate());


            if (detail.getTrainingTopic() != null) {
                row.setTrainingSample(detail.getTrainingTopic().getTrainingSample());
            }
            if (detail.getProductGroup() != null) {
                row.setProductCode(detail.getProductGroup().getProductCode());
            }


            if (detail.getTrainingPlanDetail().getEmployee() != null) {
                row.setEmployeeName(detail.getTrainingPlanDetail().getEmployee().getFullName());
                row.setEmployeeCode(detail.getTrainingPlanDetail().getEmployee().getEmployeeCode());
                row.setProcessName(detail.getTrainingPlanDetail().getProcess().getName());
                row.setClassification(String.valueOf(detail.getTrainingPlanDetail().getProcess().getClassification()));
            }

            row.setTimeIn(detail.getTimeIn());
            row.setTimeOut(detail.getTimeOut());
            row.setIsPass(detail.getIsPass());
            row.setNote(detail.getNote());


            if (detail.getSignatureProIn() != null) {

                User proIn = userRepository.findById(detail.getSignatureProIn()).orElse(null);
                if (proIn != null) row.setSignatureProInName(proIn.getFullName());
            }

            if (detail.getSignatureFiIn() != null) {
                row.setSignatureFiInName(detail.getSignatureFiIn().getFullName());
            }

            if (detail.getSignatureProOut() != null) {
                User proOut = userRepository.findById(detail.getSignatureProOut()).orElse(null);
                if (proOut != null) row.setSignatureProOutName(proOut.getFullName());
            }

            if (detail.getSignatureFiOut() != null) {
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
        TrainingResult result = trainingResultRepository.findById(resultId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kết quả ID: " + resultId));

        if (result.getStatus() == ReportStatus.ON_GOING) {
            throw new IllegalStateException("Kết quả này đã được gửi duyệt trước đó.");
        }

        boolean allSigned = result.getDetails().stream().allMatch(this::isFullSigned);
        if (!allSigned) {
            throw new IllegalArgumentException("Không thể gửi duyệt. Vui lòng đảm bảo tất cả các hạng mục đã đủ 4 chữ ký (PRO & FI).");
        }

//        // 3. Cập nhật thông tin gửi duyệt
//        result.setStatus(ReportStatus.ON_GOING);
//
//
//
//        trainingResultRepository.save(result);
    }

}
