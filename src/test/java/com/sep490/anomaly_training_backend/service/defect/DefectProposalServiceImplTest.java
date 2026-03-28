package com.sep490.anomaly_training_backend.service.defect;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalDetailRequest;
import com.sep490.anomaly_training_backend.dto.request.DefectProposalRequest;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalResponse;
import com.sep490.anomaly_training_backend.dto.response.defect.DefectProposalUpdateResponse;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ProposalType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.DefectProposalDetailMapper;
import com.sep490.anomaly_training_backend.mapper.DefectProposalMapper;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.defect.impl.DefectProposalServiceImpl;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefectProposalServiceImpl Tests")
class DefectProposalServiceImplTest {

    @Mock
    private DefectProposalRepository defectProposalRepository;

    @Mock
    private DefectRepository defectRepository;

    @Mock
    private DefectProposalMapper defectProposalMapper;

    @Mock
    private DefectProposalDetailMapper defectProposalDetailMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductLineRepository productLineRepository;

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private DefectProposalDetailRepository defectProposalDetailRepository;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DefectProposalServiceImpl service;

    private User testUser;
    private ProductLine testProductLine;
    private Process testProcess;
    private Defect testDefect;
    private Product testProduct;
    private DefectProposal testProposal;
    private DefectProposalDetail testDetail;
    private Role testRole;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = org.mockito.Mockito.mock(HttpServletRequest.class);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleCode("ROLE_TEAM_LEADER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRoles(Set.of(testRole));

        testProductLine = new ProductLine();
        testProductLine.setId(1L);
        testProductLine.setName("ProductLine 1");

        testProcess = new Process();
        testProcess.setId(1L);
        testProcess.setName("Process 1");

        testDefect = new Defect();
        testDefect.setId(1L);
        testDefect.setDefectCode("DEF001");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCode("PROD001");

        testProposal = new DefectProposal();
        testProposal.setId(1L);
        testProposal.setProductLine(testProductLine);
        testProposal.setStatus(ReportStatus.DRAFT);
        testProposal.setCreatedBy("testuser");

        testDetail = new DefectProposalDetail();
        testDetail.setId(1L);
        testDetail.setDefectProposal(testProposal);
        testDetail.setDefect(testDefect);
        testDetail.setProduct(testProduct);
        testDetail.setProcess(testProcess);
        testDetail.setProposalType(ProposalType.CREATE);
        testDetail.setDefectDescription("Test defect");
        testDetail.setDetectedDate(LocalDate.now());
        testDetail.setDefectType(DefectType.DEFECTIVE_GOODS);
        testDetail.setDeleteFlag(false);
    }

    @Nested
    @DisplayName("getDefectProposalByProductLine Tests")
    class GetDefectProposalByProductLineTests {

        @Test
        @DisplayName("Should return proposals for TEAM_LEADER role")
        void testGetProposalsForTeamLeader() {
            List<DefectProposal> mockProposals = Arrays.asList(testProposal);
            DefectProposalResponse mockResponse = DefectProposalResponse.builder().build();

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(defectProposalRepository.findByProductLineIdAndCreatedByOrderByCreatedAtDesc(1L, "testuser"))
                    .thenReturn(mockProposals);
            when(defectProposalMapper.toResponse(testProposal, userRepository))
                    .thenReturn(mockResponse);

            List<DefectProposalResponse> result = service.getDefectProposalByProductLine(1L, "testuser");

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository).findByUsername("testuser");
            verify(defectProposalRepository).findByProductLineIdAndCreatedByOrderByCreatedAtDesc(1L, "testuser");
        }

        @Test
        @DisplayName("Should return proposals for non-TEAM_LEADER role (Supervisor/Manager)")
        void testGetProposalsForSupervisor() {
            Role supervisorRole = new Role();
            supervisorRole.setRoleCode("ROLE_SUPERVISOR");
            testUser.setRoles(Set.of(supervisorRole));

            List<DefectProposal> mockProposals = Arrays.asList(testProposal);
            DefectProposalResponse mockResponse = DefectProposalResponse.builder().build();

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(defectProposalRepository.findByProductLineForSupervisorAndManagerOrderByCreatedAtDesc(1L))
                    .thenReturn(mockProposals);
            when(defectProposalMapper.toResponse(testProposal, userRepository))
                    .thenReturn(mockResponse);

            List<DefectProposalResponse> result = service.getDefectProposalByProductLine(1L, "testuser");

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(defectProposalRepository).findByProductLineForSupervisorAndManagerOrderByCreatedAtDesc(1L);
        }

        @Test
        @DisplayName("Should throw USER_NOT_FOUND when user doesn't exist")
        void testUserNotFound() {
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> {
                service.getDefectProposalByProductLine(1L, "nonexistent");
            });
        }

        @Test
        @DisplayName("Should throw ROLE_NOT_FOUND when user has no roles")
        void testRoleNotFound() {
            testUser.setRoles(new HashSet<>());

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            assertThrows(AppException.class, () -> {
                service.getDefectProposalByProductLine(1L, "testuser");
            });
        }

        @Test
        @DisplayName("Should return empty list when no proposals found")
        void testReturnEmptyList() {
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(defectProposalRepository.findByProductLineIdAndCreatedByOrderByCreatedAtDesc(1L, "testuser"))
                    .thenReturn(new ArrayList<>());

            List<DefectProposalResponse> result = service.getDefectProposalByProductLine(1L, "testuser");

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("createDefectProposalDraft Tests")
    class CreateDefectProposalDraftTests {

        @Test
        @DisplayName("Should create draft successfully")
        void testCreateDraftSuccess() {
            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setProductLineId(1L);

            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("Test defect");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);

            request.setListDetail(Arrays.asList(detailRequest));

            when(productLineRepository.findById(1L))
                    .thenReturn(Optional.of(testProductLine));
            when(defectProposalRepository.save(any(DefectProposal.class)))
                    .thenReturn(testProposal);
            when(processRepository.findById(1L))
                    .thenReturn(Optional.of(testProcess));
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);

            assertDoesNotThrow(() -> service.createDefectProposalDraft(request, testUser));

            verify(productLineRepository).findById(1L);
            verify(defectProposalRepository).save(any(DefectProposal.class));
        }

        @Test
        @DisplayName("Should throw PRODUCT_LINE_NOT_FOUND")
        void testProductLineNotFound() {
            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setProductLineId(999L);
            request.setListDetail(Arrays.asList(new DefectProposalDetailRequest()));

            when(productLineRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.createDefectProposalDraft(request, testUser));
        }
    }

    @Nested
    @DisplayName("deleteDefectProposal Tests")
    class DeleteDefectProposalTests {

        @Test
        @DisplayName("Should delete proposal by setting deleteFlag")
        void testDeleteSuccess() {
            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            service.deleteDefectProposal(1L);

            assertTrue(testProposal.isDeleteFlag());
            verify(defectProposalRepository).save(testProposal);
        }

        @Test
        @DisplayName("Should do nothing when proposal not found")
        void testDeleteNotFound() {
            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            service.deleteDefectProposal(999L);

            verify(defectProposalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateDefectProposal Tests")
    class UpdateDefectProposalTests {

        @Test
        @DisplayName("Should update proposal successfully")
        void testUpdateSuccess() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.UPDATE);
            detailRequest.setDefectDescription("Updated defect");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            DefectProposalUpdateResponse response = service.updateDefectProposal(1L, request, testUser);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            verify(defectProposalRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw PROPOSAL_HAS_NO_DETAILS when details is empty")
        void testUpdateWithNoDetails() {
            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(new ArrayList<>());

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should throw PROPOSAL_HAS_NO_DETAILS when details is null")
        void testUpdateWithNullDetails() {
            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(null);

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should throw DEFECT_PROPOSAL_NOT_FOUND")
        void testUpdateProposalNotFound() {
            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(new DefectProposalDetailRequest()));

            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.updateDefectProposal(999L, request, testUser));
        }

        @Test
        @DisplayName("Should throw INVALID_DETAIL_ID_FOR_PROPOSAL when detail id doesn't belong to proposal")
        void testUpdateWithInvalidDetailId() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setDefectProposalDetailId(999L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should soft delete detail when not in request")
        void testUpdateDeleteDetail() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("New defect");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            List<DefectProposalDetail> existingDetails = Arrays.asList(testDetail);

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(existingDetails);
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            service.updateDefectProposal(1L, request, testUser);

            verify(defectProposalDetailRepository, times(2)).save(any(DefectProposalDetail.class));
        }

        @Test
        @DisplayName("Should handle update with images")
        void testUpdateWithImages() {
            MultipartFile mockFile = org.mockito.Mockito.mock(MultipartFile.class);

            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setDefectProposalDetailId(1L);
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.UPDATE);
            detailRequest.setDefectDescription("Updated");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);
            detailRequest.setImages(Arrays.asList(mockFile));

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(Arrays.asList(testDetail));
            when(defectRepository.getReferenceById(any()))
                    .thenReturn(testDefect);
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(defectProposalDetailRepository.save(testDetail))
                    .thenReturn(testDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            service.updateDefectProposal(1L, request, testUser);

            verify(attachmentService).deleteAttachments("DEFECT_PROPOSAL", 1L);
            verify(attachmentService).uploadAttachments(Arrays.asList(mockFile), "DEFECT_PROPOSAL", 1L, "testuser");
        }
    }

    @Nested
    @DisplayName("submitDefectProposalForApproval Tests")
    class SubmitDefectProposalForApprovalTests {

        @Test
        @DisplayName("Should submit proposal successfully")
        void testSubmitSuccess() {
            testProposal.setDetails(Arrays.asList(testDetail));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);
            doNothing().when(approvalService).submit(testProposal, testUser, mockRequest);

            assertDoesNotThrow(() -> service.submitDefectProposalForApproval(1L, testUser, mockRequest));

            verify(approvalService).submit(testProposal, testUser, mockRequest);
            verify(defectProposalRepository).save(testProposal);
        }

        @Test
        @DisplayName("Should throw PROPOSAL_HAS_NO_DETAILS when no details")
        void testSubmitNoDetails() {
            testProposal.setDetails(new ArrayList<>());

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.submitDefectProposalForApproval(1L, testUser, mockRequest));
        }

        @Test
        @DisplayName("Should throw MISSING_PROCESS_IN_DETAIL when process is null")
        void testSubmitMissingProcess() {
            testDetail.setProcess(null);
            testProposal.setDetails(Arrays.asList(testDetail));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.submitDefectProposalForApproval(1L, testUser, mockRequest));
        }

        @Test
        @DisplayName("Should throw MISSING_PROPOSAL_TYPE when proposal type is null")
        void testSubmitMissingProposalType() {
            testDetail.setProposalType(null);
            testProposal.setDetails(Arrays.asList(testDetail));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.submitDefectProposalForApproval(1L, testUser, mockRequest));
        }

        @Test
        @DisplayName("Should throw MISSING_DEFECT_DESCRIPTION when description is blank")
        void testSubmitMissingDescription() {
            testDetail.setDefectDescription("");
            testProposal.setDetails(Arrays.asList(testDetail));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.submitDefectProposalForApproval(1L, testUser, mockRequest));
        }

        @Test
        @DisplayName("Should throw MISSING_DETECTED_DATE when detected date is null")
        void testSubmitMissingDetectedDate() {
            testDetail.setDetectedDate(null);
            testProposal.setDetails(Arrays.asList(testDetail));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.submitDefectProposalForApproval(1L, testUser, mockRequest));
        }
    }

    @Nested
    @DisplayName("submit Tests")
    class SubmitTests {

        @Test
        @DisplayName("Should submit successfully when user is author")
        void testSubmitSuccess() {
            testProposal.setCreatedBy("testuser");

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            doNothing().when(approvalService).submit(testProposal, testUser, mockRequest);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            assertDoesNotThrow(() -> service.submit(1L, testUser, mockRequest));

            verify(approvalService).submit(testProposal, testUser, mockRequest);
        }

        @Test
        @DisplayName("Should throw ONLY_AUTHOR_CAN_EDIT when user is not author")
        void testSubmitNotAuthor() {
            testProposal.setCreatedBy("anotheruser");

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.submit(1L, testUser, mockRequest));
        }

        @Test
        @DisplayName("Should throw DEFECT_PROPOSAL_NOT_FOUND")
        void testSubmitNotFound() {
            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.submit(999L, testUser, mockRequest));
        }
    }

    @Nested
    @DisplayName("approve Tests")
    class ApproveTests {

        @Test
        @DisplayName("Should approve successfully")
        void testApproveSuccess() {
            ApproveRequest approveRequest = new ApproveRequest();
            approveRequest.setComment("Approved");

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            doNothing().when(approvalService).approve(testProposal, testUser, approveRequest, mockRequest);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            assertDoesNotThrow(() -> service.approve(1L, testUser, approveRequest, mockRequest));

            verify(approvalService).approve(testProposal, testUser, approveRequest, mockRequest);
            verify(defectProposalRepository).save(testProposal);
        }

        @Test
        @DisplayName("Should throw DEFECT_PROPOSAL_NOT_FOUND")
        void testApproveNotFound() {
            ApproveRequest approveRequest = new ApproveRequest();

            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.approve(999L, testUser, approveRequest, mockRequest));
        }
    }

    @Nested
    @DisplayName("reject Tests")
    class RejectTests {

        @Test
        @DisplayName("Should reject successfully")
        void testRejectSuccess() {
            RejectRequest rejectRequest = new RejectRequest();
            rejectRequest.setRejectReasonIds(List.of(1L, 2L));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            doNothing().when(approvalService).reject(testProposal, testUser, rejectRequest, mockRequest);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            assertDoesNotThrow(() -> service.reject(1L, testUser, rejectRequest, mockRequest));

            verify(approvalService).reject(testProposal, testUser, rejectRequest, mockRequest);
            verify(defectProposalRepository).save(testProposal);
        }

        @Test
        @DisplayName("Should throw DEFECT_PROPOSAL_NOT_FOUND")
        void testRejectNotFound() {
            RejectRequest rejectRequest = new RejectRequest();

            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.reject(999L, testUser, rejectRequest, mockRequest));
        }
    }

    @Nested
    @DisplayName("canApprove Tests")
    class CanApproveTests {

        @Test
        @DisplayName("Should return true when user can approve")
        void testCanApproveTrue() {
            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(approvalService.canApprove(testProposal, testUser))
                    .thenReturn(true);

            ResponseEntity<Boolean> response = service.canApprove(1L, testUser);

            assertTrue(response.getBody());
            assertEquals(200, response.getStatusCodeValue());
        }

        @Test
        @DisplayName("Should return false when user cannot approve")
        void testCanApproveFalse() {
            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(approvalService.canApprove(testProposal, testUser))
                    .thenReturn(false);

            ResponseEntity<Boolean> response = service.canApprove(1L, testUser);

            assertFalse(response.getBody());
        }

        @Test
        @DisplayName("Should return false when proposal not found")
        void testCanApproveNotFound() {
            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            ResponseEntity<Boolean> response = service.canApprove(999L, testUser);

            assertFalse(response.getBody());
        }

        @Test
        @DisplayName("Should return false when AppException is thrown")
        void testCanApproveException() {
            when(defectProposalRepository.findById(1L))
                    .thenThrow(new AppException(ErrorCode.DEFECT_PROPOSAL_NOT_FOUND));

            ResponseEntity<Boolean> response = service.canApprove(1L, testUser);

            assertFalse(response.getBody());
        }
    }

    @Nested
    @DisplayName("revise Tests")
    class ReviseTests {

        @Test
        @DisplayName("Should revise successfully when user is author")
        void testReviseSuccess() {
            testProposal.setCreatedBy("testuser");

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            doNothing().when(approvalService).revise(testProposal, testUser, mockRequest);

            assertDoesNotThrow(() -> service.revise(1L, testUser, mockRequest));

            verify(approvalService).revise(testProposal, testUser, mockRequest);
        }

        @Test
        @DisplayName("Should throw ONLY_AUTHOR_CAN_EDIT when user is not author")
        void testReviseNotAuthor() {
            testProposal.setCreatedBy("anotheruser");

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));

            assertThrows(AppException.class, () -> service.revise(1L, testUser, mockRequest));
        }

        @Test
        @DisplayName("Should throw DEFECT_PROPOSAL_NOT_FOUND")
        void testReviseNotFound() {
            when(defectProposalRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.revise(999L, testUser, mockRequest));
        }
    }

    @Nested
    @DisplayName("sendSubmission Tests")
    class SendSubmissionTests {

        @Test
        @DisplayName("Should send submission successfully")
        void testSendSubmissionSuccess() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.DELETE);
            detailRequest.setDefectDescription("Test defect");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setProductLineId(1L);
            request.setListDetail(Arrays.asList(detailRequest));

            when(productLineRepository.findById(1L))
                    .thenReturn(Optional.of(testProductLine));
            when(defectProposalRepository.save(any(DefectProposal.class)))
                    .thenReturn(testProposal);
            when(processRepository.findById(1L))
                    .thenReturn(Optional.of(testProcess));
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);
            doNothing().when(approvalService).submit(testProposal, testUser, mockRequest);

            assertDoesNotThrow(() -> service.sendSubmission(request, testUser, mockRequest));

            verify(approvalService).submit(any(DefectProposal.class), eq(testUser), eq(mockRequest));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null product in detail")
        void testUpdateDetailWithNullProduct() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.UPDATE);
            detailRequest.setDefectDescription("Test");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(null);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            DefectProposalUpdateResponse response = service.updateDefectProposal(1L, request, testUser);

            assertNotNull(response);
        }

        @Test
        @DisplayName("Should handle null defect in detail")
        void testUpdateDetailWithNullDefect() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("Test");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);
            detailRequest.setDefectId(null);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            testDetail.setDefect(null);

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            DefectProposalUpdateResponse response = service.updateDefectProposal(1L, request, testUser);

            assertNotNull(response);
        }

        @Test
        @DisplayName("Should handle multiple details in single proposal")
        void testMultipleDetails() {
            DefectProposalDetailRequest detail1 = new DefectProposalDetailRequest();
            detail1.setProcessId(1L);
            detail1.setProposalType(ProposalType.CREATE);
            detail1.setDefectDescription("Defect 1");
            detail1.setDetectedDate(LocalDate.now());
            detail1.setProductId(1L);

            DefectProposalDetailRequest detail2 = new DefectProposalDetailRequest();
            detail2.setProcessId(1L);
            detail2.setProposalType(ProposalType.UPDATE);
            detail2.setDefectDescription("Defect 2");
            detail2.setDetectedDate(LocalDate.now());
            detail2.setProductId(1L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setProductLineId(1L);
            request.setListDetail(Arrays.asList(detail1, detail2));

            when(productLineRepository.findById(1L))
                    .thenReturn(Optional.of(testProductLine));
            when(defectProposalRepository.save(any(DefectProposal.class)))
                    .thenReturn(testProposal);
            when(processRepository.findById(1L))
                    .thenReturn(Optional.of(testProcess));
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(testDetail);

            assertDoesNotThrow(() -> service.createDefectProposalDraft(request, testUser));

            verify(defectProposalDetailRepository, times(2)).save(any(DefectProposalDetail.class));
        }
        

        @Test
        @DisplayName("Should throw MISSING_PRODUCT_ID when productId is null in mapToEntity")
        void testMapToEntityMissingProductId() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("Test");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(null);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should throw MISSING_PROPOSAL_TYPE in mapToEntity")
        void testMapToEntityMissingProposalType() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(null);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should throw MISSING_DEFECT_DESCRIPTION in mapToEntity")
        void testMapToEntityMissingDescription() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription(null);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should throw INVALID_REQUEST_FORMAT when request is null")
        void testMapToEntityNullRequest() {
            DefectProposalDetailRequest detailRequest = null;
            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should handle defect reference get error in update")
        void testUpdateDetailHandleDefectRefError() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setDefectProposalDetailId(1L);
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("Test");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);
            detailRequest.setDefectId(1L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(Arrays.asList(testDetail));
            when(defectRepository.getReferenceById(1L))
                    .thenReturn(testDefect);
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(defectProposalDetailRepository.save(testDetail))
                    .thenReturn(testDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            DefectProposalUpdateResponse response = service.updateDefectProposal(1L, request, testUser);

            assertNotNull(response);
        }

        @Test
        @DisplayName("Should throw PRODUCT_NOT_FOUND when product doesn't exist")
        void testUpdateDetailProductNotFound() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("Test");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(999L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(productRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> service.updateDefectProposal(1L, request, testUser));
        }

        @Test
        @DisplayName("Should create new detail when defectProposalDetailId is null")
        void testUpdateCreateNewDetail() {
            DefectProposalDetailRequest detailRequest = new DefectProposalDetailRequest();
            detailRequest.setDefectProposalDetailId(null);
            detailRequest.setProcessId(1L);
            detailRequest.setProposalType(ProposalType.CREATE);
            detailRequest.setDefectDescription("New detail");
            detailRequest.setDetectedDate(LocalDate.now());
            detailRequest.setProductId(1L);

            DefectProposalRequest request = DefectProposalRequest.builder().build();
            request.setListDetail(Arrays.asList(detailRequest));

            DefectProposalDetail newDetail = new DefectProposalDetail();
            newDetail.setId(2L);

            when(defectProposalRepository.findById(1L))
                    .thenReturn(Optional.of(testProposal));
            when(defectProposalDetailRepository.findByDefectProposalIdAndDeleteFlagFalse(1L))
                    .thenReturn(new ArrayList<>());
            when(processRepository.getReferenceById(1L))
                    .thenReturn(testProcess);
            when(productRepository.findById(1L))
                    .thenReturn(Optional.of(testProduct));
            when(defectProposalDetailRepository.save(any(DefectProposalDetail.class)))
                    .thenReturn(newDetail);
            when(defectProposalRepository.save(testProposal))
                    .thenReturn(testProposal);

            DefectProposalUpdateResponse response = service.updateDefectProposal(1L, request, testUser);

            assertNotNull(response);
            verify(defectProposalDetailRepository).save(any(DefectProposalDetail.class));
        }
    }
}

