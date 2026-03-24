package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.DetailFeedbackRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.ScheduleRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.TrainingPlanUpdateRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.dto.response.PrioritizedEmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.dto.response.ProductLineResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanDetailResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanGenerationResponse;
import com.sep490.anomaly_training_backend.dto.response.TrainingPlanResponse;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.TrainingPlanDetailStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.PrioritySnapshotMapper;
import com.sep490.anomaly_training_backend.mapper.TrainingPlanMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.PriorityPolicyRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotDetailRepository;
import com.sep490.anomaly_training_backend.repository.PrioritySnapshotRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanHistoryRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanSpecialDayRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TrainingPlanService;
import com.sep490.anomaly_training_backend.service.TrainingResultService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.priority.TrainingPlanScheduleGenerationService;
import com.sep490.anomaly_training_backend.service.priority.impl.PriorityScoringServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceImplTest {

    // ── Repositories ──
    @Mock
    private TrainingPlanRepository trainingPlanRepository;
    @Mock
    private TrainingPlanDetailRepository trainingPlanDetailRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TrainingPlanHistoryRepository trainingPlanHistoryRepository;
    @Mock
    private ProductLineRepository productLineRepository;
    @Mock
    private EmployeeSkillRepository employeeSkillRepository;
    @Mock
    private TrainingResultDetailRepository trainingResultDetailRepository;
    @Mock
    private TrainingResultRepository trainingResultRepository;
    @Mock
    private PriorityPolicyRepository policyRepository;
    @Mock
    private PrioritySnapshotRepository prioritySnapshotRepository;
    @Mock
    private PrioritySnapshotDetailRepository prioritySnapshotDetailRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private RejectReasonRepository rejectReasonRepository;
    @Mock
    private RequiredActionRepository requiredActionRepository;
    @Mock
    private TrainingPlanSpecialDayRepository trainingPlanSpecialDayRepository;

    // ── Services ──
    @Mock
    private ApprovalService approvalService;
    @Mock
    private TrainingResultService trainingResultService;
    @Mock
    private TrainingPlanScheduleGenerationService trainingPlanScheduleGenerationService;

    // ── Mappers & Others ──
    @Mock
    private TrainingPlanMapper planMapper;
    @Mock
    private PrioritySnapshotMapper prioritySnapshotMapper;
    @Mock
    private PriorityScoringServiceImpl priorityScoringService;

    @InjectMocks
    private TrainingPlanServiceImpl trainingPlanServiceImpl;

    // Gọi test qua interface
    private TrainingPlanService trainingPlanService;

    // ── Common test data ──
    private User testUser;
    private Employee testEmployee;
    private Team testTeam;
    private Group testGroup;
    private ProductLine testProductLine;
    private TrainingPlan testPlan;

    @BeforeEach
    void setUp() {
        trainingPlanService = trainingPlanServiceImpl;

        // Build common test objects
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("Group A");

        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("Team A");
        testTeam.setGroup(testGroup);

        testProductLine = new ProductLine();
        testProductLine.setId(1L);
        testProductLine.setName("Line A");
        testProductLine.setCode("LA");
        testProductLine.setGroup(testGroup);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setEmployeeCode("EMP001");
        testEmployee.setFullName("Nguyen Van A");
        testEmployee.setStatus(EmployeeStatus.ACTIVE);

        Role tlRole = new Role();
        tlRole.setId(1L);
        tlRole.setRoleCode("ROLE_TEAM_LEADER");
        tlRole.setIsActive(true);

        testUser = User.builder()
                .id(1L)
                .username("tl_user01")
                .fullName("Team Leader 01")
                .email("tl@test.com")
                .employeeCode("TL001")
                .roles(new HashSet<>(Set.of(tlRole)))
                .build();

        testPlan = new TrainingPlan();
        testPlan.setId(1L);
        testPlan.setTitle("Test Plan");
        testPlan.setStatus(ReportStatus.DRAFT);
        testPlan.setStartDate(LocalDate.of(2025, 1, 1));
        testPlan.setEndDate(LocalDate.of(2025, 12, 31));
        testPlan.setTeam(testTeam);
        testPlan.setLine(testProductLine);
        testPlan.setDetails(new ArrayList<>());
        testPlan.setSpecialDays(new ArrayList<>());
        testPlan.setCreatedBy("tl_user01");
        testPlan.setCurrentVersion(1);
    }

    /**
     * Helper: mock chuỗi toGenerationResponse
     */
    private void mockToGenerationResponse(TrainingPlan plan) {
        TrainingPlanResponse planRes = new TrainingPlanResponse();
        planRes.setId(plan.getId());
        planRes.setTitle(plan.getTitle());
        planRes.setDetails(new ArrayList<>());

        lenient().when(planMapper.toResponse(plan)).thenReturn(planRes);
        lenient().when(prioritySnapshotRepository.findByTrainingPlanId(plan.getId()))
                .thenReturn(Optional.empty());
        lenient().when(prioritySnapshotMapper.toResponse(null)).thenReturn(null);
        lenient().when(userRepository.findByUsername(plan.getCreatedBy()))
                .thenReturn(Optional.of(testUser));
    }

    /**
     * Helper: tạo TrainingPlanDetail
     */
    private TrainingPlanDetail buildDetail(Long id, Employee emp, LocalDate planned, String batchId) {
        TrainingPlanDetail d = new TrainingPlanDetail();
        d.setId(id);
        d.setTrainingPlan(testPlan);
        d.setEmployee(emp);
        d.setTargetMonth(planned != null ? planned.withDayOfMonth(1) : null);
        d.setPlannedDate(planned);
        d.setStatus(TrainingPlanDetailStatus.PENDING);
        d.setBatchId(batchId);
        return d;
    }

    /**
     * Helper: mock SecurityContext
     */
    private void mockSecurityContext(String username) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(username);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NHÓM 1: QUERY TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPlanDetail")
    class GetPlanDetailTests {

        @Test
        @DisplayName("[Normal] Trả về plan khi ID hợp lệ")
        void getPlanDetail_validId_returnsResponse() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            mockToGenerationResponse(testPlan);

            TrainingPlanGenerationResponse result = trainingPlanService.getPlanDetail(1L);

            assertNotNull(result);
            assertNotNull(result.getTrainingPlan());
            assertEquals(1L, result.getTrainingPlan().getId());
        }

        @Test
        @DisplayName("[Abnormal] Throw TRAINING_PLAN_NOT_FOUND khi ID không tồn tại")
        void getPlanDetail_notFound_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.getPlanDetail(999L));
            assertEquals(ErrorCode.TRAINING_PLAN_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getAllPlans")
    class GetAllPlansTests {

        @Test
        @DisplayName("[Normal] ROLE_FINAL_INSPECTION - lineId null - trả danh sách plans")
        void getAllPlans_finalInspection_noLineId() {
            User fiUser = User.builder().id(2L).username("fi_user").fullName("FI").email("fi@test.com")
                    .employeeCode("FI01").roles(new HashSet<>()).build();
            Role fiRole = new Role();
            fiRole.setRoleCode("ROLE_FINAL_INSPECTION");
            fiRole.setIsActive(true);
            fiUser.getRoles().add(fiRole);

            when(teamRepository.findByFinalInspectionId(2L)).thenReturn(List.of(testTeam));
            when(trainingPlanRepository.findAllByGroupIdsAndDeleteFlagFalse(anyList()))
                    .thenReturn(List.of(testPlan));
            mockToGenerationResponse(testPlan);

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(fiUser, null);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("[Normal] ROLE_FINAL_INSPECTION - có lineId")
        void getAllPlans_finalInspection_withLineId() {
            User fiUser = User.builder().id(2L).username("fi_user").fullName("FI").email("fi@test.com")
                    .employeeCode("FI01").roles(new HashSet<>()).build();
            Role fiRole = new Role();
            fiRole.setRoleCode("ROLE_FINAL_INSPECTION");
            fiRole.setIsActive(true);
            fiUser.getRoles().add(fiRole);

            when(teamRepository.findByFinalInspectionId(2L)).thenReturn(List.of(testTeam));
            when(trainingPlanRepository.findAllByGroupIdsAndLineIdAndDeleteFlagFalse(anyList(), eq(1L)))
                    .thenReturn(List.of(testPlan));
            mockToGenerationResponse(testPlan);

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(fiUser, 1L);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("[Boundary] ROLE_FINAL_INSPECTION - empty teams → empty list")
        void getAllPlans_finalInspection_noTeams() {
            User fiUser = User.builder().id(2L).username("fi_user").fullName("FI").email("fi@test.com")
                    .employeeCode("FI01").roles(new HashSet<>()).build();
            Role fiRole = new Role();
            fiRole.setRoleCode("ROLE_FINAL_INSPECTION");
            fiRole.setIsActive(true);
            fiUser.getRoles().add(fiRole);

            when(teamRepository.findByFinalInspectionId(2L)).thenReturn(Collections.emptyList());

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(fiUser, null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("[Normal] ROLE_MANAGER - lineId null")
        void getAllPlans_manager_noLineId() {
            User mgrUser = User.builder().id(3L).username("mgr_user").fullName("Mgr").email("mgr@test.com")
                    .employeeCode("MGR01").roles(new HashSet<>()).build();
            Role mgrRole = new Role();
            mgrRole.setRoleCode("ROLE_MANAGER");
            mgrRole.setIsActive(true);
            mgrUser.getRoles().add(mgrRole);

            when(trainingPlanRepository.findAllByManagerAndDeleteFlagFalse(eq(3L), anyList()))
                    .thenReturn(List.of(testPlan));
            mockToGenerationResponse(testPlan);

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(mgrUser, null);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("[Normal] ROLE_SUPERVISOR - lineId null")
        void getAllPlans_supervisor_noLineId() {
            User svUser = User.builder().id(4L).username("sv_user").fullName("SV").email("sv@test.com")
                    .employeeCode("SV01").roles(new HashSet<>()).build();
            Role svRole = new Role();
            svRole.setRoleCode("ROLE_SUPERVISOR");
            svRole.setIsActive(true);
            svUser.getRoles().add(svRole);

            when(groupRepository.findBySupervisorId(4L)).thenReturn(List.of(testGroup));
            when(trainingPlanRepository.findAllByGroupIdsAndDeleteFlagFalse(anyList()))
                    .thenReturn(List.of(testPlan));
            mockToGenerationResponse(testPlan);

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(svUser, null);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("[Boundary] ROLE_SUPERVISOR - empty groups → empty list")
        void getAllPlans_supervisor_noGroups() {
            User svUser = User.builder().id(4L).username("sv_user").fullName("SV").email("sv@test.com")
                    .employeeCode("SV01").roles(new HashSet<>()).build();
            Role svRole = new Role();
            svRole.setRoleCode("ROLE_SUPERVISOR");
            svRole.setIsActive(true);
            svUser.getRoles().add(svRole);

            when(groupRepository.findBySupervisorId(4L)).thenReturn(Collections.emptyList());

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(svUser, null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("[Normal] Default role (creator) - lineId null")
        void getAllPlans_defaultRole_noLineId() {
            when(trainingPlanRepository.findByCreatedByAndDeleteFlagFalse("tl_user01"))
                    .thenReturn(List.of(testPlan));
            mockToGenerationResponse(testPlan);

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(testUser, null);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("[Normal] Default role (creator) - có lineId")
        void getAllPlans_defaultRole_withLineId() {
            when(trainingPlanRepository.findByCreatedByAndLineIdAndDeleteFlagFalse("tl_user01", 1L))
                    .thenReturn(List.of(testPlan));
            mockToGenerationResponse(testPlan);

            List<TrainingPlanGenerationResponse> result = trainingPlanService.getAllPlans(testUser, 1L);
            assertFalse(result.isEmpty());
        }
    }

//    @Nested
//    @DisplayName("getRejectedPlans")
//    class GetRejectedPlansTests {
//
//        @Test
//        @DisplayName("[Normal] Trả danh sách plans bị rejected")
//        void getRejectedPlans_returnsRejectedPlans() {
//            testPlan.setStatus(ReportStatus.REJECTED_BY_SV);
//            when(trainingPlanRepository.findByStatusInAndDeleteFlagFalse(anyList()))
//                    .thenReturn(List.of(testPlan));
//            mockToGenerationResponse(testPlan);
//
//            List<TrainingPlanGenerationResponse> result = trainingPlanService.getRejectedPlans();
//            assertFalse(result.isEmpty());
//        }
//
//        @Test
//        @DisplayName("[Boundary] Không có plan nào bị rejected → empty list")
//        void getRejectedPlans_empty() {
//            when(trainingPlanRepository.findByStatusInAndDeleteFlagFalse(anyList()))
//                    .thenReturn(Collections.emptyList());
//
//            List<TrainingPlanGenerationResponse> result = trainingPlanService.getRejectedPlans();
//            assertTrue(result.isEmpty());
//        }
//    }

    // ═══════════════════════════════════════════════════════════════════
    //  NHÓM 2: MUTATION TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addDetail")
    class AddDetailTests {

        @Test
        @DisplayName("[Normal] Thêm detail vào plan DRAFT thành công")
        void addDetail_draftPlan_success() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            TrainingPlanDetailResponse detailRes = new TrainingPlanDetailResponse();
            detailRes.setEmployeeId(1L);
            when(planMapper.toDetailResponse(any())).thenReturn(detailRes);

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 6, 1));
            schedule.setPlannedDay(15);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setEmployeeId(1L);
            request.setSchedules(List.of(schedule));

            TrainingPlanDetailResponse result = trainingPlanService.addDetail(1L, request);

            assertNotNull(result);
            verify(trainingPlanRepository).save(any());
        }

        @Test
        @DisplayName("[Normal] Thêm detail vào plan APPROVED → trigger regenerateResultDetails")
        void addDetail_approvedPlan_triggersRegenerate() {
            testPlan.setStatus(ReportStatus.APPROVED);
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);
            when(trainingResultRepository.findByTrainingPlanId(1L)).thenReturn(Collections.emptyList());

            TrainingPlanDetailResponse detailRes = new TrainingPlanDetailResponse();
            when(planMapper.toDetailResponse(any())).thenReturn(detailRes);

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 6, 1));
            schedule.setPlannedDay(15);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setEmployeeId(1L);
            request.setSchedules(List.of(schedule));

            trainingPlanService.addDetail(1L, request);

            verify(trainingResultRepository).findByTrainingPlanId(1L);
        }

        @Test
        @DisplayName("[Abnormal] Plan WAITING_SV → throw INVALID_TRAINING_PLAN_STATUS")
        void addDetail_waitingSv_throwsException() {
            testPlan.setStatus(ReportStatus.WAITING_SV);
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.addDetail(1L, request));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Plan WAITING_MANAGER → throw INVALID_TRAINING_PLAN_STATUS")
        void addDetail_waitingManager_throwsException() {
            testPlan.setStatus(ReportStatus.WAITING_MANAGER);
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.addDetail(1L, request));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Employee không tồn tại → throw EMPLOYEE_NOT_FOUND")
        void addDetail_employeeNotFound_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 6, 1));
            schedule.setPlannedDay(15);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setEmployeeId(999L);
            request.setSchedules(List.of(schedule));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.addDetail(1L, request));
            assertEquals(ErrorCode.EMPLOYEE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Schedule rỗng (tất cả plannedDay ≤ 0) → throw MISSING_SCHEDULE")
        void addDetail_allSchedulesInvalid_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 6, 1));
            schedule.setPlannedDay(0); // ≤ 0

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setEmployeeId(1L);
            request.setSchedules(List.of(schedule));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.addDetail(1L, request));
            assertEquals(ErrorCode.MISSING_SCHEDULE, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Boundary] plannedDay = 31 cho tháng 2 → throw INVALID_DAY_OF_MONTH")
        void addDetail_invalidDayOfMonth_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 2, 1)); // Feb 2025 = 28 days
            schedule.setPlannedDay(31);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setEmployeeId(1L);
            request.setSchedules(List.of(schedule));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.addDetail(1L, request));
            assertEquals(ErrorCode.INVALID_DAY_OF_MONTH, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Plan không tồn tại → throw TRAINING_PLAN_NOT_FOUND")
        void addDetail_planNotFound_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.addDetail(999L, request));
            assertEquals(ErrorCode.TRAINING_PLAN_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("updateDetail")
    class UpdateDetailTests {

        @Test
        @DisplayName("[Normal] Update detail thành công")
        void updateDetail_success() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            TrainingPlanDetailResponse detailRes = new TrainingPlanDetailResponse();
            detailRes.setId(10L);
            when(planMapper.toDetailResponse(detail)).thenReturn(detailRes);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setNote("Updated note");

            TrainingPlanDetailResponse result = trainingPlanService.updateDetail(1L, 10L, request);

            assertNotNull(result);
        }

        @Test
        @DisplayName("[Abnormal] Plan WAITING_SV → throw INVALID_TRAINING_PLAN_STATUS")
        void updateDetail_waitingSv_throwsException() {
            testPlan.setStatus(ReportStatus.WAITING_SV);
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updateDetail(1L, 10L, request));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Detail không tồn tại → throw TRAINING_PLAN_DETAIL_NOT_FOUND")
        void updateDetail_detailNotFound_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updateDetail(1L, 999L, request));
            assertEquals(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] Update employee và schedule")
        void updateDetail_updateEmployeeAndSchedule() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);

            Employee newEmp = new Employee();
            newEmp.setId(2L);
            newEmp.setEmployeeCode("EMP002");
            newEmp.setFullName("Nguyen Van B");

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(newEmp));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            TrainingPlanDetailResponse detailRes = new TrainingPlanDetailResponse();
            when(planMapper.toDetailResponse(detail)).thenReturn(detailRes);

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 7, 1));
            schedule.setPlannedDay(20);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setEmployeeId(2L);
            request.setSchedules(List.of(schedule));

            trainingPlanService.updateDetail(1L, 10L, request);

            assertEquals(newEmp, detail.getEmployee());
            assertEquals(LocalDate.of(2025, 7, 20), detail.getPlannedDate());
        }

        @Test
        @DisplayName("[Boundary] Planned day 31 cho tháng 2 → throw INVALID_DAY_OF_MONTH")
        void updateDetail_invalidDayOfMonth_throwsException() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 2, 1));
            schedule.setPlannedDay(31);

            TrainingPlanDetailRequest request = new TrainingPlanDetailRequest();
            request.setSchedules(List.of(schedule));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updateDetail(1L, 10L, request));
            assertEquals(ErrorCode.INVALID_DAY_OF_MONTH, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("deletePlan")
    class DeletePlanTests {

        @Test
        @DisplayName("[Normal] Delete plan DRAFT bởi creator thành công")
        void deletePlan_draftByCreator_success() {
            mockSecurityContext("tl_user01");
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(prioritySnapshotRepository.findByTrainingPlanId(1L)).thenReturn(Optional.empty());

            trainingPlanService.deletePlan(1L);

            verify(trainingPlanRepository).delete(testPlan);
        }

        @Test
        @DisplayName("[Abnormal] Plan APPROVED → throw INVALID_TRAINING_PLAN_STATUS")
        void deletePlan_approved_throwsException() {
            testPlan.setStatus(ReportStatus.APPROVED);
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.deletePlan(1L));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Plan WAITING_SV → throw INVALID_TRAINING_PLAN_STATUS")
        void deletePlan_waitingSv_throwsException() {
            testPlan.setStatus(ReportStatus.WAITING_SV);
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.deletePlan(1L));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Không phải creator và không phải team leader → INSUFFICIENT_PERMISSION")
        void deletePlan_noPermission_throwsException() {
            mockSecurityContext("other_user");
            User otherUser = User.builder().id(99L).username("other_user").fullName("O")
                    .email("o@t.com").employeeCode("O01").roles(new HashSet<>()).build();

            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(userRepository.findByUsername("other_user")).thenReturn(Optional.of(otherUser));
            when(teamRepository.findAllByTeamLeaderId(99L)).thenReturn(Collections.emptyList());

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.deletePlan(1L));
            assertEquals(ErrorCode.INSUFFICIENT_PERMISSION, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] Delete plan REJECTED_BY_SV bởi team leader thành công")
        void deletePlan_rejectedByTeamLeader_success() {
            testPlan.setStatus(ReportStatus.REJECTED_BY_SV);
            mockSecurityContext("other_user");
            User otherUser = User.builder().id(99L).username("other_user").fullName("O")
                    .email("o@t.com").employeeCode("O01").roles(new HashSet<>()).build();

            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(userRepository.findByUsername("other_user")).thenReturn(Optional.of(otherUser));
            when(teamRepository.findAllByTeamLeaderId(99L)).thenReturn(List.of(testTeam));
            when(prioritySnapshotRepository.findByTrainingPlanId(1L)).thenReturn(Optional.empty());

            trainingPlanService.deletePlan(1L);

            verify(trainingPlanRepository).delete(testPlan);
        }
    }

    @Nested
    @DisplayName("deleteDetail")
    class DeleteDetailTests {

        @Test
        @DisplayName("[Normal] Delete detail PENDING từ plan DRAFT thành công")
        void deleteDetail_pendingFromDraft_success() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            trainingPlanService.deleteDetail(1L, 10L);

            verify(trainingResultDetailRepository).deleteByTrainingPlanDetailId(10L);
            verify(trainingPlanRepository).save(any());
        }

        @Test
        @DisplayName("[Abnormal] Plan APPROVED → throw INVALID_TRAINING_PLAN_STATUS")
        void deleteDetail_approvedPlan_throwsException() {
            testPlan.setStatus(ReportStatus.APPROVED);
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.deleteDetail(1L, 10L));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Detail DONE → throw INVALID_TRAINING_PLAN_STATUS")
        void deleteDetail_doneDetail_throwsException() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            detail.setStatus(TrainingPlanDetailStatus.DONE);
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.deleteDetail(1L, 10L));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] Detail không tồn tại → throw TRAINING_PLAN_DETAIL_NOT_FOUND")
        void deleteDetail_notFound_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.deleteDetail(1L, 999L));
            assertEquals(ErrorCode.TRAINING_PLAN_DETAIL_NOT_FOUND, ex.getErrorCode());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NHÓM 2 (tiếp): updatePlan
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updatePlan")
    class UpdatePlanTests {

        @Test
        @DisplayName("[Normal] Update header (title, note) thành công")
        void updatePlan_updateHeader_success() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);
            mockToGenerationResponse(testPlan);

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setTitle("New Title");
            request.setNote("New Note");

            TrainingPlanGenerationResponse result = trainingPlanService.updatePlan(1L, request);
            assertNotNull(result);
            assertEquals("New Title", testPlan.getTitle());
        }

        @Test
        @DisplayName("[Abnormal] Plan WAITING_SV → throw INVALID_TRAINING_PLAN_STATUS")
        void updatePlan_waitingSv_throwsException() {
            testPlan.setStatus(ReportStatus.WAITING_SV);
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updatePlan(1L, request));
            assertEquals(ErrorCode.INVALID_TRAINING_PLAN_STATUS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] endDate < startDate → throw INVALID_DATE_RANGE")
        void updatePlan_invalidDateRange_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setStartDate(LocalDate.of(2025, 12, 31));
            request.setEndDate(LocalDate.of(2025, 1, 1));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updatePlan(1L, request));
            assertEquals(ErrorCode.INVALID_DATE_RANGE, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] Detail action ADD thành công")
        void updatePlan_addAction_success() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);
            mockToGenerationResponse(testPlan);

            ScheduleRequest schedule = new ScheduleRequest();
            schedule.setTargetMonth(LocalDate.of(2025, 6, 1));
            schedule.setPlannedDay(15);

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
            action.setAction(TrainingPlanUpdateRequest.ActionType.ADD);
            action.setEmployeeId(1L);
            action.setSchedules(List.of(schedule));

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            trainingPlanService.updatePlan(1L, request);
            assertFalse(testPlan.getDetails().isEmpty());
        }

        @Test
        @DisplayName("[Abnormal] Detail action null → throw MISSING_ACTION_IN_DETAIL")
        void updatePlan_missingAction_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updatePlan(1L, request));
            assertEquals(ErrorCode.MISSING_ACTION_IN_DETAIL, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] ADD thiếu employeeId → throw MISSING_EMPLOYEE_ID")
        void updatePlan_addMissingEmployeeId_throwsException() {
            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
            action.setAction(TrainingPlanUpdateRequest.ActionType.ADD);

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updatePlan(1L, request));
            assertEquals(ErrorCode.MISSING_EMPLOYEE_ID, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] DELETE từ DRAFT - xóa thật")
        void updatePlan_deleteAction_draft() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);
            mockToGenerationResponse(testPlan);

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
            action.setAction(TrainingPlanUpdateRequest.ActionType.DELETE);
            action.setDetailId(10L);

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            trainingPlanService.updatePlan(1L, request);
            verify(trainingResultDetailRepository).deleteByTrainingPlanDetailId(10L);
        }

        @Test
        @DisplayName("[Normal] DELETE từ APPROVED → đánh dấu MISS")
        void updatePlan_deleteAction_approved_marksMiss() {
            testPlan.setStatus(ReportStatus.APPROVED);
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);
            when(trainingResultRepository.findByTrainingPlanId(1L)).thenReturn(Collections.emptyList());
            mockToGenerationResponse(testPlan);

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
            action.setAction(TrainingPlanUpdateRequest.ActionType.DELETE);
            action.setDetailId(10L);

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            trainingPlanService.updatePlan(1L, request);
            assertEquals(TrainingPlanDetailStatus.MISS, detail.getStatus());
        }

        @Test
        @DisplayName("[Abnormal] DELETE detail DONE khi APPROVED → throw CANNOT_DELETE_COMPLETED_DETAIL")
        void updatePlan_deleteCompletedDetail_throwsException() {
            testPlan.setStatus(ReportStatus.APPROVED);
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            detail.setStatus(TrainingPlanDetailStatus.DONE);
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
            action.setAction(TrainingPlanUpdateRequest.ActionType.DELETE);
            action.setDetailId(10L);

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updatePlan(1L, request));
            assertEquals(ErrorCode.CANNOT_DELETE_COMPLETED_DETAIL, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] UPDATE detail DONE khi APPROVED → throw CANNOT_UPDATE_COMPLETED_DETAIL")
        void updatePlan_updateCompletedDetail_throwsException() {
            testPlan.setStatus(ReportStatus.APPROVED);
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            detail.setStatus(TrainingPlanDetailStatus.DONE);
            testPlan.getDetails().add(detail);

            when(trainingPlanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testPlan));

            TrainingPlanUpdateRequest.DetailAction action = new TrainingPlanUpdateRequest.DetailAction();
            action.setAction(TrainingPlanUpdateRequest.ActionType.UPDATE);
            action.setDetailId(10L);
            action.setNote("New note");

            TrainingPlanUpdateRequest request = new TrainingPlanUpdateRequest();
            request.setDetails(List.of(action));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.updatePlan(1L, request));
            assertEquals(ErrorCode.CANNOT_UPDATE_COMPLETED_DETAIL, ex.getErrorCode());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NHÓM 4: LOOKUP TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Lookup Functions")
    class LookupTests {

        @Test
        @DisplayName("[Normal] getMyManagedGroups - trả danh sách groups")
        void getMyManagedGroups_success() {
            mockSecurityContext("tl_user01");
            when(userRepository.findByUsername("tl_user01")).thenReturn(Optional.of(testUser));
            when(teamRepository.findAllByTeamLeaderId(1L)).thenReturn(List.of(testTeam));

            List<GroupResponse> result = trainingPlanService.getMyManagedGroups();
            assertFalse(result.isEmpty());
            assertEquals("Group A", result.get(0).getName());
        }

        @Test
        @DisplayName("[Boundary] getMyManagedGroups - no teams → empty list")
        void getMyManagedGroups_noTeams() {
            mockSecurityContext("tl_user01");
            when(userRepository.findByUsername("tl_user01")).thenReturn(Optional.of(testUser));
            when(teamRepository.findAllByTeamLeaderId(1L)).thenReturn(Collections.emptyList());

            List<GroupResponse> result = trainingPlanService.getMyManagedGroups();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("[Normal] getProcessesByProductLine - trả processes")
        void getProcessesByProductLine_success() {
            Process process = new Process();
            process.setId(1L);
            process.setCode("P001");
            process.setName("Process 1");

            when(processRepository.findByProductLineIdAndDeleteFlagFalse(1L)).thenReturn(List.of(process));

            List<ProcessResponse> result = trainingPlanService.getProcessesByProductLine(1L);
            assertFalse(result.isEmpty());
            assertEquals("Process 1", result.get(0).getName());
        }

        @Test
        @DisplayName("[Boundary] getProcessesByProductLine - empty")
        void getProcessesByProductLine_empty() {
            when(processRepository.findByProductLineIdAndDeleteFlagFalse(1L)).thenReturn(Collections.emptyList());
            assertTrue(trainingPlanService.getProcessesByProductLine(1L).isEmpty());
        }

        @Test
        @DisplayName("[Normal] getProductLinesByGroupId - trả product lines")
        void getProductLinesByGroupId_success() {
            when(productLineRepository.findByGroupIdAndDeleteFlagFalse(1L)).thenReturn(List.of(testProductLine));

            List<ProductLineResponse> result = trainingPlanService.getProductLinesByGroupId(1L);
            assertFalse(result.isEmpty());
            assertEquals("Line A", result.get(0).getName());
        }

        @Test
        @DisplayName("[Normal] getEmployeesNotInPlan - trả employees không trong plan")
        void getEmployeesNotInPlan_success() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(1L)).thenReturn(Collections.emptyList());
            when(prioritySnapshotRepository.findByTrainingPlanId(1L)).thenReturn(Optional.empty());
            when(trainingResultDetailRepository.findLatestByEmployeeIds(anyList())).thenReturn(Collections.emptyList());

            List<PrioritizedEmployeeResponse> result = trainingPlanService.getEmployeesNotInPlan(1L);
            assertFalse(result.isEmpty());
            assertEquals("Nguyen Van A", result.get(0).getFullName());
        }

        @Test
        @DisplayName("[Boundary] getEmployeesNotInPlan - groupId null → empty")
        void getEmployeesNotInPlan_noGroup() {
            TrainingPlan planNoGroup = new TrainingPlan();
            planNoGroup.setId(2L);
            planNoGroup.setLine(null);
            planNoGroup.setTeam(null);
            when(trainingPlanRepository.findById(2L)).thenReturn(Optional.of(planNoGroup));

            assertTrue(trainingPlanService.getEmployeesNotInPlan(2L).isEmpty());
        }

        @Test
        @DisplayName("[Normal] getEmployeesInTeams - trả employees trong team")
        void getEmployeesInTeams_success() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanDetailRepository.findEmployeeIdsByTrainingPlanId(1L)).thenReturn(Collections.emptyList());
            when(prioritySnapshotRepository.findByTrainingPlanId(1L)).thenReturn(Optional.empty());
            when(trainingResultDetailRepository.findLatestByEmployeeIds(anyList())).thenReturn(Collections.emptyList());

            List<PrioritizedEmployeeResponse> result = trainingPlanService.getEmployeesInTeams(1L);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("[Boundary] getEmployeesInTeams - teamId null → empty")
        void getEmployeesInTeams_noTeam() {
            TrainingPlan planNoTeam = new TrainingPlan();
            planNoTeam.setId(2L);
            planNoTeam.setTeam(null);
            when(trainingPlanRepository.findById(2L)).thenReturn(Optional.of(planNoTeam));

            assertTrue(trainingPlanService.getEmployeesInTeams(2L).isEmpty());
        }

        @Test
        @DisplayName("[Abnormal] getEmployeesNotInPlan - plan not found → throw")
        void getEmployeesNotInPlan_notFound() {
            when(trainingPlanRepository.findById(999L)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.getEmployeesNotInPlan(999L));
            assertEquals(ErrorCode.TRAINING_PLAN_NOT_FOUND, ex.getErrorCode());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NHÓM 5: APPROVAL WORKFLOW TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Approval Workflow")
    class ApprovalWorkflowTests {

        private HttpServletRequest mockRequest;

        @BeforeEach
        void setUpApproval() {
            mockRequest = mock(HttpServletRequest.class);
        }

        @Test
        @DisplayName("[Normal] submitPlanForApproval - thành công")
        void submitPlanForApproval_success() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);
            testPlan.setTitle("Valid Title");

            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest);

            verify(approvalService).submit(eq(testPlan), eq(testUser), eq(mockRequest));
            assertNotNull(testPlan.getFormCode());
        }

        @Test
        @DisplayName("[Abnormal] submitPlanForApproval - no details → PLAN_HAS_NO_DETAILS")
        void submitPlanForApproval_noDetails() {
            testPlan.setTitle("Valid Title");
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest));
            assertEquals(ErrorCode.PLAN_HAS_NO_DETAILS, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] submitPlanForApproval - title null → MISSING_PLAN_TITLE")
        void submitPlanForApproval_noTitle() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);
            testPlan.setTitle(null);
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest));
            assertEquals(ErrorCode.MISSING_PLAN_TITLE, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] submitPlanForApproval - endDate < startDate → INVALID_DATE_RANGE")
        void submitPlanForApproval_invalidDateRange() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);
            testPlan.setTitle("Valid");
            testPlan.setStartDate(LocalDate.of(2025, 12, 31));
            testPlan.setEndDate(LocalDate.of(2025, 1, 1));
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest));
            assertEquals(ErrorCode.INVALID_DATE_RANGE, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] submitPlanForApproval - detail thiếu employee → MISSING_EMPLOYEE_IN_DETAIL")
        void submitPlanForApproval_missingEmployee() {
            TrainingPlanDetail detail = buildDetail(10L, null, LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);
            testPlan.setTitle("Valid");
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest));
            assertEquals(ErrorCode.MISSING_EMPLOYEE_IN_DETAIL, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] submitPlanForApproval - plannedDate ngoài range → PLANNED_DATE_OUT_OF_RANGE")
        void submitPlanForApproval_dateOutOfRange() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2026, 6, 15), "batch-1");
            testPlan.getDetails().add(detail);
            testPlan.setTitle("Valid");
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest));
            assertEquals(ErrorCode.PLANNED_DATE_OUT_OF_RANGE, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Abnormal] submitPlanForApproval - duplicate schedule → DUPLICATE_TRAINING_SCHEDULE")
        void submitPlanForApproval_duplicateSchedule() {
            TrainingPlanDetail d1 = buildDetail(10L, testEmployee, LocalDate.of(2025, 6, 15), "batch-1");
            TrainingPlanDetail d2 = buildDetail(11L, testEmployee, LocalDate.of(2025, 6, 15), "batch-1");
            testPlan.getDetails().add(d1);
            testPlan.getDetails().add(d2);
            testPlan.setTitle("Valid");
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.submitPlanForApproval(1L, testUser, mockRequest));
            assertEquals(ErrorCode.DUPLICATE_TRAINING_SCHEDULE, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] submit - thành công")
        void submit_success() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            trainingPlanService.submit(1L, testUser, mockRequest);
            verify(approvalService).submit(testPlan, testUser, mockRequest);
        }

        @Test
        @DisplayName("[Normal] revise - bởi author thành công")
        void revise_byAuthor_success() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);
            when(trainingPlanHistoryRepository.save(any())).thenReturn(null);

            trainingPlanService.revise(1L, testUser, mockRequest);

            verify(approvalService).revise(testPlan, testUser, mockRequest);
            verify(trainingPlanHistoryRepository).save(any());
        }

        @Test
        @DisplayName("[Abnormal] revise - không phải author → ONLY_AUTHOR_CAN_EDIT")
        void revise_notAuthor() {
            User otherUser = User.builder().id(99L).username("other").fullName("O")
                    .email("o@t.com").employeeCode("O01").roles(new HashSet<>()).build();
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.revise(1L, otherUser, mockRequest));
            assertEquals(ErrorCode.ONLY_AUTHOR_CAN_EDIT, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] approve - chưa final → không tạo result")
        void approve_notFinal() {
            testPlan.setStatus(ReportStatus.WAITING_MANAGER);
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            trainingPlanService.approve(1L, testUser, new ApproveRequest(), mockRequest);

            verify(approvalService).approve(eq(testPlan), eq(testUser), any(), eq(mockRequest));
            verify(trainingResultService, never()).generateTrainingResult(anyLong());
        }

        @Test
        @DisplayName("[Normal] approve - final → generateTrainingResult")
        void approve_final_generatesResult() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            doAnswer(inv -> {
                testPlan.setStatus(ReportStatus.APPROVED);
                return null;
            }).when(approvalService).approve(any(), any(), any(), any());
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            trainingPlanService.approve(1L, testUser, new ApproveRequest(), mockRequest);
            verify(trainingResultService).generateTrainingResult(1L);
        }

        @Test
        @DisplayName("[Normal] reject - thành công")
        void reject_success() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(trainingPlanRepository.save(any())).thenReturn(testPlan);

            trainingPlanService.reject(1L, testUser, new RejectRequest(), mockRequest);
            verify(approvalService).reject(eq(testPlan), eq(testUser), any(), eq(mockRequest));
        }

        @Test
        @DisplayName("[Normal] canApprove → true")
        void canApprove_true() {
            when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(approvalService.canApprove(testPlan, testUser)).thenReturn(true);

            ResponseEntity<Boolean> result = trainingPlanService.canApprove(1L, testUser);
            assertTrue(result.getBody());
        }

        @Test
        @DisplayName("[Abnormal] canApprove - exception → false")
        void canApprove_exception_returnsFalse() {
            when(trainingPlanRepository.findById(999L))
                    .thenThrow(new AppException(ErrorCode.TRAINING_PLAN_NOT_FOUND));

            ResponseEntity<Boolean> result = trainingPlanService.canApprove(999L, testUser);
            assertFalse(result.getBody());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NHÓM 6: FEEDBACK TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Feedback")
    class FeedbackTests {

        @Test
        @DisplayName("[Normal] saveFeedback - lưu feedback thành công")
        void saveFeedback_success() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            when(trainingPlanDetailRepository.findByIdAndDeleteFlagFalse(10L)).thenReturn(Optional.of(detail));
            when(rejectReasonRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
            when(trainingPlanDetailRepository.save(any())).thenReturn(detail);

            DetailFeedbackRequest request = new DetailFeedbackRequest();
            request.setComment("Some comment");
            request.setRejectReasonIds(List.of(1L));

            trainingPlanService.saveFeedback(10L, request, testUser);

            verify(trainingPlanDetailRepository).save(detail);
            assertNotNull(detail.getRejectFeedback());
        }

        @Test
        @DisplayName("[Boundary] saveFeedback - empty → clear feedback")
        void saveFeedback_emptyFeedback_clears() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            when(trainingPlanDetailRepository.findByIdAndDeleteFlagFalse(10L)).thenReturn(Optional.of(detail));
            when(trainingPlanDetailRepository.save(any())).thenReturn(detail);

            DetailFeedbackRequest request = new DetailFeedbackRequest();
            trainingPlanService.saveFeedback(10L, request, testUser);

            assertNull(detail.getRejectFeedback());
        }

        @Test
        @DisplayName("[Abnormal] saveFeedback - detail not found → throw")
        void saveFeedback_notFound() {
            when(trainingPlanDetailRepository.findByIdAndDeleteFlagFalse(999L)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> trainingPlanService.saveFeedback(999L, new DetailFeedbackRequest(), testUser));
            assertEquals(ErrorCode.PROPOSAL_DETAIL_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("[Normal] clearFeedback - xóa toàn bộ feedback")
        void clearFeedback_success() {
            TrainingPlanDetail detail = buildDetail(10L, testEmployee,
                    LocalDate.of(2025, 6, 15), "batch-1");
            when(trainingPlanDetailRepository.findByTrainingPlanIdAndDeleteFlagFalse(1L))
                    .thenReturn(List.of(detail));

            trainingPlanService.clearFeedback(1L);

            assertNull(detail.getRejectFeedback());
            verify(trainingPlanDetailRepository).saveAll(anyList());
        }
    }
}

