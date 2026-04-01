//package com.sep490.anomaly_training_backend.service.impl;
//
//import ch.qos.logback.core.util.COWArrayList;
//import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
//import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
//import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
//import com.sep490.anomaly_training_backend.enums.OAuthProvider;
//import com.sep490.anomaly_training_backend.exception.AppException;
//import com.sep490.anomaly_training_backend.exception.ErrorCode;
//import com.sep490.anomaly_training_backend.mapper.EmployeeMapper;
//import com.sep490.anomaly_training_backend.mapper.EmployeeMapperImpl;
//import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
//import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
//import com.sep490.anomaly_training_backend.mapper.ProcessMapperImpl;
//import com.sep490.anomaly_training_backend.model.Employee;
//import com.sep490.anomaly_training_backend.model.Group;
//import com.sep490.anomaly_training_backend.model.Section;
//import com.sep490.anomaly_training_backend.model.Team;
//import com.sep490.anomaly_training_backend.model.User;
//import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
//import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
//import com.sep490.anomaly_training_backend.repository.ProcessRepository;
//import com.sep490.anomaly_training_backend.repository.TeamRepository;
//import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
//import com.sep490.anomaly_training_backend.repository.UserRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.aot.DisabledInAotMode;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.isA;
//import static org.mockito.Mockito.atLeast;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ContextConfiguration(classes = {EmployeeServiceImpl.class})
//@DisabledInAotMode
//@ExtendWith(SpringExtension.class)
//class EmployeeServiceImplDiffblueTest {
//    @MockitoBean
//    private EmployeeMapper employeeMapper;
//
//    @MockitoBean
//    private EmployeeRepository employeeRepository;
//
//    @Autowired
//    private EmployeeServiceImpl employeeServiceImpl;
//
//    @MockitoBean
//    private EmployeeSkillMapper employeeSkillMapper;
//
//    @MockitoBean
//    private EmployeeSkillRepository employeeSkillRepository;
//
//    @MockitoBean
//    private ProcessMapper processMapper;
//
//    @MockitoBean
//    private ProcessRepository processRepository;
//
//    @MockitoBean
//    private TeamRepository teamRepository;
//
//    @MockitoBean
//    private TrainingResultDetailRepository trainingResultDetailRepository;
//
//    @MockitoBean
//    private UserRepository userRepository;
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest)")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        when(employeeRepository.save(Mockito.<Employee>any())).thenReturn(employee);
//
//        Employee employee2 = new Employee();
//        employee2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee2.setDeleteFlag(true);
//        employee2.setEmployeeCode("Employee Code");
//        employee2.setFullName("Dr Jane Doe");
//        employee2.setId(1L);
//        employee2.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee2.setSkills(new ArrayList<>());
//        employee2.setStatus(EmployeeStatus.ACTIVE);
//        employee2.setTeams(new ArrayList<>());
//        employee2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee2.setUpdatedBy("2020-03-01");
//        when(employeeMapper.toDTO(Mockito.<Employee>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//        when(employeeMapper.toEntity(Mockito.<EmployeeRequest>any())).thenReturn(employee2);
//        when(teamRepository.existsById(Mockito.<Long>any())).thenReturn(true);
//
//        ArrayList<Long> resultLongList = new ArrayList<>();
//        resultLongList.add(1L);
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(resultLongList);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(request).getEmployeeCode();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//        verify(employeeMapper).toDTO(isA(Employee.class));
//        verify(employeeMapper).toEntity(isA(EmployeeRequest.class));
//        verify(teamRepository).existsById(1L);
//        verify(employeeRepository).save(isA(Employee.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest)")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee2() {
//        // Arrange
//        when(employeeRepository.save(Mockito.<Employee>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        when(employeeMapper.toEntity(Mockito.<EmployeeRequest>any())).thenReturn(employee);
//        when(teamRepository.existsById(Mockito.<Long>any())).thenReturn(true);
//
//        ArrayList<Long> resultLongList = new ArrayList<>();
//        resultLongList.add(1L);
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(resultLongList);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(request).getEmployeeCode();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//        verify(employeeMapper).toEntity(isA(EmployeeRequest.class));
//        verify(teamRepository).existsById(1L);
//        verify(employeeRepository).save(isA(Employee.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest)")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee3() {
//        // Arrange
//        when(employeeMapper.toEntity(Mockito.<EmployeeRequest>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//        when(teamRepository.existsById(Mockito.<Long>any())).thenReturn(true);
//
//        ArrayList<Long> resultLongList = new ArrayList<>();
//        resultLongList.add(1L);
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(resultLongList);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(request).getEmployeeCode();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//        verify(employeeMapper).toEntity(isA(EmployeeRequest.class));
//        verify(teamRepository).existsById(1L);
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest)")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee4() {
//        // Arrange
//        when(employeeRepository.existsByEmployeeCode(Mockito.<String>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//
//        EmployeeRequest request = new EmployeeRequest();
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(employeeRepository).existsByEmployeeCode("Employee Code");
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest)")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee5() {
//        // Arrange
//        when(teamRepository.existsById(Mockito.<Long>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//
//        ArrayList<Long> resultLongList = new ArrayList<>();
//        resultLongList.add(1L);
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(resultLongList);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(request).getEmployeeCode();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//        verify(teamRepository).existsById(1L);
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <ul>
//     *   <li>Given {@link AppException#AppException(ErrorCode)} with errorCode is {@code SUCCESS}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName(
//            "Test createEmployee(EmployeeRequest); given AppException(ErrorCode) with errorCode is 'SUCCESS'")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee_givenAppExceptionWithErrorCodeIsSuccess() {
//        // Arrange
//        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
//        EmployeeMapperImpl employeeMapper = new EmployeeMapperImpl();
//        TeamRepository teamRepository = mock(TeamRepository.class);
//        ProcessRepository processRepository = mock(ProcessRepository.class);
//        TrainingResultDetailRepository trainingResultDetailRepository =
//                mock(TrainingResultDetailRepository.class);
//        EmployeeSkillRepository employeeSkillRepository = mock(EmployeeSkillRepository.class);
//        UserRepository userRepository = mock(UserRepository.class);
//        EmployeeSkillMapper employeeSkillMapper = mock(EmployeeSkillMapper.class);
//        EmployeeServiceImpl employeeServiceImpl =
//                new EmployeeServiceImpl(
//                        employeeRepository,
//                        employeeMapper,
//                        teamRepository,
//                        processRepository,
//                        trainingResultDetailRepository,
//                        employeeSkillRepository,
//                        userRepository,
//                        employeeSkillMapper,
//                        new ProcessMapperImpl());
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getFullName()).thenThrow(new AppException(ErrorCode.SUCCESS));
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(null);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(request, atLeast(1)).getEmployeeCode();
//        verify(request).getFullName();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <ul>
//     *   <li>Given {@link EmployeeRepository} {@link EmployeeRepository#existsByEmployeeCode(String)}
//     *       return {@code true}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName(
//            "Test createEmployee(EmployeeRequest); given EmployeeRepository existsByEmployeeCode(String) return 'true'")
//    void testCreateEmployee_givenEmployeeRepositoryExistsByEmployeeCodeReturnTrue() {
//        // Arrange
//        when(employeeRepository.existsByEmployeeCode(Mockito.<String>any())).thenReturn(true);
//        EmployeeRequest request = new EmployeeRequest();
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.createEmployee(request));
//        verify(employeeRepository).existsByEmployeeCode("Employee Code");
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <ul>
//     *   <li>Given {@link User#User()} CreatedBy is {@code ,}.
//     *   <li>Then return TeamName is {@code Name, Name}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName(
//            "Test createEmployee(EmployeeRequest); given User() CreatedBy is ','; then return TeamName is 'Name, Name'")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee_givenUserCreatedByIsComma_thenReturnTeamNameIsNameName() {
//        // Arrange
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//
//        User finalInspection2 = new User();
//        finalInspection2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection2.setCreatedBy(", ");
//        finalInspection2.setDeleteFlag(false);
//        finalInspection2.setEmail("john.smith@example.org");
//        finalInspection2.setEmployeeCode("Employee Code");
//        finalInspection2.setFullName("Mr John Smith");
//        finalInspection2.setId(2L);
//        finalInspection2.setIsActive(false);
//        finalInspection2.setOauthProvider(OAuthProvider.MICROSOFT);
//        finalInspection2.setOauthProviderId(", ");
//        finalInspection2.setPasswordHash("Password Hash");
//        finalInspection2.setRequirePasswordChange(false);
//        finalInspection2.setRoles(new HashSet<>());
//        finalInspection2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection2.setUpdatedBy("2020/03/01");
//        finalInspection2.setUsername(", ");
//
//        User manager2 = new User();
//        manager2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager2.setCreatedBy(", ");
//        manager2.setDeleteFlag(false);
//        manager2.setEmail("john.smith@example.org");
//        manager2.setEmployeeCode("Employee Code");
//        manager2.setFullName("Mr John Smith");
//        manager2.setId(2L);
//        manager2.setIsActive(false);
//        manager2.setOauthProvider(OAuthProvider.MICROSOFT);
//        manager2.setOauthProviderId(", ");
//        manager2.setPasswordHash("Password Hash");
//        manager2.setRequirePasswordChange(false);
//        manager2.setRoles(new HashSet<>());
//        manager2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager2.setUpdatedBy("2020/03/01");
//        manager2.setUsername(", ");
//
//        Section section2 = new Section();
//        section2.setCode("Code");
//        section2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section2.setCreatedBy(", ");
//        section2.setDeleteFlag(false);
//        section2.setGroups(new ArrayList<>());
//        section2.setId(2L);
//        section2.setManager(manager2);
//        section2.setName("Name");
//        section2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section2.setUpdatedBy("2020/03/01");
//
//        User supervisor2 = new User();
//        supervisor2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor2.setCreatedBy(", ");
//        supervisor2.setDeleteFlag(false);
//        supervisor2.setEmail("john.smith@example.org");
//        supervisor2.setEmployeeCode("Employee Code");
//        supervisor2.setFullName("Mr John Smith");
//        supervisor2.setId(2L);
//        supervisor2.setIsActive(false);
//        supervisor2.setOauthProvider(OAuthProvider.MICROSOFT);
//        supervisor2.setOauthProviderId(", ");
//        supervisor2.setPasswordHash("Password Hash");
//        supervisor2.setRequirePasswordChange(false);
//        supervisor2.setRoles(new HashSet<>());
//        supervisor2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor2.setUpdatedBy("2020/03/01");
//        supervisor2.setUsername(", ");
//
//        Group group2 = new Group();
//        group2.setCode("Code");
//        group2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group2.setCreatedBy(", ");
//        group2.setDeleteFlag(false);
//        group2.setId(2L);
//        group2.setName("Name");
//        group2.setSection(section2);
//        group2.setSupervisor(supervisor2);
//        group2.setTeams(new ArrayList<>());
//        group2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group2.setUpdatedBy("2020/03/01");
//
//        User teamLeader2 = new User();
//        teamLeader2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader2.setCreatedBy(", ");
//        teamLeader2.setDeleteFlag(false);
//        teamLeader2.setEmail("john.smith@example.org");
//        teamLeader2.setEmployeeCode("Employee Code");
//        teamLeader2.setFullName("Mr John Smith");
//        teamLeader2.setId(2L);
//        teamLeader2.setIsActive(false);
//        teamLeader2.setOauthProvider(OAuthProvider.MICROSOFT);
//        teamLeader2.setOauthProviderId(", ");
//        teamLeader2.setPasswordHash("Password Hash");
//        teamLeader2.setRequirePasswordChange(false);
//        teamLeader2.setRoles(new HashSet<>());
//        teamLeader2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader2.setUpdatedBy("2020/03/01");
//        teamLeader2.setUsername(", ");
//
//        Team team2 = new Team();
//        team2.setCode("Code");
//        team2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team2.setCreatedBy(", ");
//        team2.setDeleteFlag(false);
//        team2.setEmployees(new ArrayList<>());
//        team2.setFinalInspection(finalInspection2);
//        team2.setGroup(group2);
//        team2.setId(2L);
//        team2.setName("Name");
//        team2.setTeamLeader(teamLeader2);
//        team2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team2.setUpdatedBy("2020/03/01");
//
//        ArrayList<Team> teams = new ArrayList<>();
//        teams.add(team2);
//        teams.add(team);
//
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(teams);
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
//        when(employeeRepository.save(Mockito.<Employee>any())).thenReturn(employee);
//        EmployeeMapperImpl employeeMapper = new EmployeeMapperImpl();
//        TeamRepository teamRepository = mock(TeamRepository.class);
//        ProcessRepository processRepository = mock(ProcessRepository.class);
//        TrainingResultDetailRepository trainingResultDetailRepository =
//                mock(TrainingResultDetailRepository.class);
//        EmployeeSkillRepository employeeSkillRepository = mock(EmployeeSkillRepository.class);
//        UserRepository userRepository = mock(UserRepository.class);
//        EmployeeSkillMapper employeeSkillMapper = mock(EmployeeSkillMapper.class);
//        EmployeeServiceImpl employeeServiceImpl =
//                new EmployeeServiceImpl(
//                        employeeRepository,
//                        employeeMapper,
//                        teamRepository,
//                        processRepository,
//                        trainingResultDetailRepository,
//                        employeeSkillRepository,
//                        userRepository,
//                        employeeSkillMapper,
//                        new ProcessMapperImpl());
//
//        EmployeeRequest request = new EmployeeRequest();
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setEmployeeCode(null);
//        request.setTeamIds(null);
//
//        // Act
//        EmployeeResponse actualCreateEmployeeResult = employeeServiceImpl.createEmployee(request);
//
//        // Assert
//        verify(employeeRepository).save(isA(Employee.class));
//        assertEquals("Name, Name", actualCreateEmployeeResult.getTeamName());
//        List<Long> teamIds = actualCreateEmployeeResult.getTeamIds();
//        assertEquals(2, teamIds.size());
//        assertEquals(1L, teamIds.get(1).longValue());
//        assertEquals(2L, teamIds.get(0).longValue());
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <ul>
//     *   <li>Then return {@link EmployeeResponse} (default constructor).
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName(
//            "Test createEmployee(EmployeeRequest); then return EmployeeResponse (default constructor)")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee_thenReturnEmployeeResponse() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        when(employeeRepository.save(Mockito.<Employee>any())).thenReturn(employee);
//
//        Employee employee2 = new Employee();
//        employee2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee2.setDeleteFlag(true);
//        employee2.setEmployeeCode("Employee Code");
//        employee2.setFullName("Dr Jane Doe");
//        employee2.setId(1L);
//        employee2.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee2.setSkills(new ArrayList<>());
//        employee2.setStatus(EmployeeStatus.ACTIVE);
//        employee2.setTeams(new ArrayList<>());
//        employee2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee2.setUpdatedBy("2020-03-01");
//
//        EmployeeResponse employeeResponse = new EmployeeResponse();
//        employeeResponse.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employeeResponse.setEmployeeCode("Employee Code");
//        employeeResponse.setFullName("Dr Jane Doe");
//        employeeResponse.setGroupName("Group Name");
//        employeeResponse.setId(1L);
//        employeeResponse.setRoles(new ArrayList<>());
//        employeeResponse.setSectionName("Section Name");
//        employeeResponse.setSkills(new ArrayList<>());
//        employeeResponse.setStatus(EmployeeStatus.ACTIVE);
//        employeeResponse.setTeamIds(new ArrayList<>());
//        employeeResponse.setTeamName("Team Name");
//        employeeResponse.setTotalFail(1);
//        employeeResponse.setTotalTraining(1);
//        employeeResponse.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        when(employeeMapper.toDTO(Mockito.<Employee>any())).thenReturn(employeeResponse);
//        when(employeeMapper.toEntity(Mockito.<EmployeeRequest>any())).thenReturn(employee2);
//        when(teamRepository.existsById(Mockito.<Long>any())).thenReturn(true);
//
//        ArrayList<Long> resultLongList = new ArrayList<>();
//        resultLongList.add(1L);
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(resultLongList);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act
//        EmployeeResponse actualCreateEmployeeResult = employeeServiceImpl.createEmployee(request);
//
//        // Assert
//        verify(request).getEmployeeCode();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//        verify(employeeMapper).toDTO(isA(Employee.class));
//        verify(employeeMapper).toEntity(isA(EmployeeRequest.class));
//        verify(teamRepository).existsById(1L);
//        verify(employeeRepository).save(isA(Employee.class));
//        assertSame(employeeResponse, actualCreateEmployeeResult);
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <ul>
//     *   <li>Then return GroupName is empty string.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest); then return GroupName is empty string")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee_thenReturnGroupNameIsEmptyString() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
//        when(employeeRepository.save(Mockito.<Employee>any())).thenReturn(employee);
//        EmployeeMapperImpl employeeMapper = new EmployeeMapperImpl();
//        TeamRepository teamRepository = mock(TeamRepository.class);
//        ProcessRepository processRepository = mock(ProcessRepository.class);
//        TrainingResultDetailRepository trainingResultDetailRepository =
//                mock(TrainingResultDetailRepository.class);
//        EmployeeSkillRepository employeeSkillRepository = mock(EmployeeSkillRepository.class);
//        UserRepository userRepository = mock(UserRepository.class);
//        EmployeeSkillMapper employeeSkillMapper = mock(EmployeeSkillMapper.class);
//        EmployeeServiceImpl employeeServiceImpl =
//                new EmployeeServiceImpl(
//                        employeeRepository,
//                        employeeMapper,
//                        teamRepository,
//                        processRepository,
//                        trainingResultDetailRepository,
//                        employeeSkillRepository,
//                        userRepository,
//                        employeeSkillMapper,
//                        new ProcessMapperImpl());
//        EmployeeRequest request = mock(EmployeeRequest.class);
//        when(request.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
//        when(request.getFullName()).thenReturn("Dr Jane Doe");
//        when(request.getEmployeeCode()).thenReturn(null);
//        when(request.getTeamIds()).thenReturn(null);
//        doNothing().when(request).setEmployeeCode(Mockito.<String>any());
//        doNothing().when(request).setFullName(Mockito.<String>any());
//        doNothing().when(request).setStatus(Mockito.<EmployeeStatus>any());
//        doNothing().when(request).setTeamIds(Mockito.<List<Long>>any());
//        request.setEmployeeCode("Employee Code");
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setTeamIds(new ArrayList<>());
//
//        // Act
//        EmployeeResponse actualCreateEmployeeResult = employeeServiceImpl.createEmployee(request);
//
//        // Assert
//        verify(request, atLeast(1)).getEmployeeCode();
//        verify(request).getFullName();
//        verify(request).getStatus();
//        verify(request, atLeast(1)).getTeamIds();
//        verify(request).setEmployeeCode("Employee Code");
//        verify(request).setFullName("Dr Jane Doe");
//        verify(request).setStatus(EmployeeStatus.ACTIVE);
//        verify(request).setTeamIds(isA(List.class));
//        verify(employeeRepository).save(isA(Employee.class));
//        assertEquals("", actualCreateEmployeeResult.getGroupName());
//        assertEquals("", actualCreateEmployeeResult.getTeamName());
//        assertNull(actualCreateEmployeeResult.getTotalFail());
//        assertNull(actualCreateEmployeeResult.getTotalTraining());
//        assertNull(actualCreateEmployeeResult.getSectionName());
//        assertNull(actualCreateEmployeeResult.getRoles());
//        assertTrue(actualCreateEmployeeResult.getTeamIds().isEmpty());
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}.
//     *
//     * <ul>
//     *   <li>Then return GroupName is {@code Name}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#createEmployee(EmployeeRequest)}
//     */
//    @Test
//    @DisplayName("Test createEmployee(EmployeeRequest); then return GroupName is 'Name'")
//    @Tag("MaintainedByDiffblue")
//    void testCreateEmployee_thenReturnGroupNameIsName() {
//        // Arrange
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//
//        ArrayList<Team> teams = new ArrayList<>();
//        teams.add(team);
//
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(teams);
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
//        when(employeeRepository.save(Mockito.<Employee>any())).thenReturn(employee);
//        EmployeeMapperImpl employeeMapper = new EmployeeMapperImpl();
//        TeamRepository teamRepository = mock(TeamRepository.class);
//        ProcessRepository processRepository = mock(ProcessRepository.class);
//        TrainingResultDetailRepository trainingResultDetailRepository =
//                mock(TrainingResultDetailRepository.class);
//        EmployeeSkillRepository employeeSkillRepository = mock(EmployeeSkillRepository.class);
//        UserRepository userRepository = mock(UserRepository.class);
//        EmployeeSkillMapper employeeSkillMapper = mock(EmployeeSkillMapper.class);
//        EmployeeServiceImpl employeeServiceImpl =
//                new EmployeeServiceImpl(
//                        employeeRepository,
//                        employeeMapper,
//                        teamRepository,
//                        processRepository,
//                        trainingResultDetailRepository,
//                        employeeSkillRepository,
//                        userRepository,
//                        employeeSkillMapper,
//                        new ProcessMapperImpl());
//
//        EmployeeRequest request = new EmployeeRequest();
//        request.setFullName("Dr Jane Doe");
//        request.setStatus(EmployeeStatus.ACTIVE);
//        request.setEmployeeCode(null);
//        request.setTeamIds(null);
//
//        // Act
//        EmployeeResponse actualCreateEmployeeResult = employeeServiceImpl.createEmployee(request);
//
//        // Assert
//        verify(employeeRepository).save(isA(Employee.class));
//        assertEquals("Name", actualCreateEmployeeResult.getGroupName());
//        assertEquals("Name", actualCreateEmployeeResult.getTeamName());
//        List<Long> teamIds = actualCreateEmployeeResult.getTeamIds();
//        assertEquals(1, teamIds.size());
//        assertEquals(1L, teamIds.get(0).longValue());
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName("Test addEmployeesToTeam(Long, List)")
//    void testAddEmployeesToTeam() {
//        // Arrange
//        when(teamRepository.findById(Mockito.<Long>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//        ArrayList<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(1L);
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.addEmployeesToTeam(1L, employeeIds));
//        verify(teamRepository).findById(1L);
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName("Test addEmployeesToTeam(Long, List)")
//    @Tag("MaintainedByDiffblue")
//    void testAddEmployeesToTeam2() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//
//        ArrayList<Employee> employeeList = new ArrayList<>();
//        employeeList.add(employee);
//        when(employeeRepository.saveAll(Mockito.<Iterable<Employee>>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//        when(employeeRepository.findAllById(Mockito.<Iterable<Long>>any())).thenReturn(employeeList);
//
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//        Optional<Team> ofResult = Optional.of(team);
//        when(teamRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        ArrayList<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(1L);
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.addEmployeesToTeam(1L, employeeIds));
//        verify(teamRepository).findById(1L);
//        verify(employeeRepository).findAllById(isA(Iterable.class));
//        verify(employeeRepository).saveAll(isA(Iterable.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName("Test addEmployeesToTeam(Long, List)")
//    @Tag("MaintainedByDiffblue")
//    void testAddEmployeesToTeam3() {
//        // Arrange
//        when(employeeRepository.findAllById(Mockito.<Iterable<Long>>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//        Optional<Team> ofResult = Optional.of(team);
//        when(teamRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        ArrayList<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(1L);
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.addEmployeesToTeam(1L, employeeIds));
//        verify(teamRepository).findById(1L);
//        verify(employeeRepository).findAllById(isA(Iterable.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <ul>
//     *   <li>Given {@link ArrayList#ArrayList()} add {@link Team#Team()}.
//     *   <li>Then calls {@link EmployeeRepository#saveAll(Iterable)}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName(
//            "Test addEmployeesToTeam(Long, List); given ArrayList() add Team(); then calls saveAll(Iterable)")
//    @Tag("MaintainedByDiffblue")
//    void testAddEmployeesToTeam_givenArrayListAddTeam_thenCallsSaveAll() {
//        // Arrange
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//
//        ArrayList<Team> teams = new ArrayList<>();
//        teams.add(team);
//
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(teams);
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//
//        ArrayList<Employee> employeeList = new ArrayList<>();
//        employeeList.add(employee);
//        when(employeeRepository.saveAll(Mockito.<Iterable<Employee>>any()))
//                .thenReturn(new ArrayList<>());
//        when(employeeRepository.findAllById(Mockito.<Iterable<Long>>any())).thenReturn(employeeList);
//
//        User finalInspection2 = new User();
//        finalInspection2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection2.setDeleteFlag(true);
//        finalInspection2.setEmail("jane.doe@example.org");
//        finalInspection2.setEmployeeCode("Employee Code");
//        finalInspection2.setFullName("Dr Jane Doe");
//        finalInspection2.setId(1L);
//        finalInspection2.setIsActive(true);
//        finalInspection2.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection2.setOauthProviderId("42");
//        finalInspection2.setPasswordHash("Password Hash");
//        finalInspection2.setRequirePasswordChange(true);
//        finalInspection2.setRoles(new HashSet<>());
//        finalInspection2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection2.setUpdatedBy("2020-03-01");
//        finalInspection2.setUsername("janedoe");
//
//        User manager2 = new User();
//        manager2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager2.setDeleteFlag(true);
//        manager2.setEmail("jane.doe@example.org");
//        manager2.setEmployeeCode("Employee Code");
//        manager2.setFullName("Dr Jane Doe");
//        manager2.setId(1L);
//        manager2.setIsActive(true);
//        manager2.setOauthProvider(OAuthProvider.LOCAL);
//        manager2.setOauthProviderId("42");
//        manager2.setPasswordHash("Password Hash");
//        manager2.setRequirePasswordChange(true);
//        manager2.setRoles(new HashSet<>());
//        manager2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager2.setUpdatedBy("2020-03-01");
//        manager2.setUsername("janedoe");
//
//        Section section2 = new Section();
//        section2.setCode("Code");
//        section2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section2.setDeleteFlag(true);
//        section2.setGroups(new ArrayList<>());
//        section2.setId(1L);
//        section2.setManager(manager2);
//        section2.setName("Name");
//        section2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section2.setUpdatedBy("2020-03-01");
//
//        User supervisor2 = new User();
//        supervisor2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor2.setDeleteFlag(true);
//        supervisor2.setEmail("jane.doe@example.org");
//        supervisor2.setEmployeeCode("Employee Code");
//        supervisor2.setFullName("Dr Jane Doe");
//        supervisor2.setId(1L);
//        supervisor2.setIsActive(true);
//        supervisor2.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor2.setOauthProviderId("42");
//        supervisor2.setPasswordHash("Password Hash");
//        supervisor2.setRequirePasswordChange(true);
//        supervisor2.setRoles(new HashSet<>());
//        supervisor2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor2.setUpdatedBy("2020-03-01");
//        supervisor2.setUsername("janedoe");
//
//        Group group2 = new Group();
//        group2.setCode("Code");
//        group2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group2.setDeleteFlag(true);
//        group2.setId(1L);
//        group2.setName("Name");
//        group2.setSection(section2);
//        group2.setSupervisor(supervisor2);
//        group2.setTeams(new ArrayList<>());
//        group2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group2.setUpdatedBy("2020-03-01");
//
//        User teamLeader2 = new User();
//        teamLeader2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader2.setDeleteFlag(true);
//        teamLeader2.setEmail("jane.doe@example.org");
//        teamLeader2.setEmployeeCode("Employee Code");
//        teamLeader2.setFullName("Dr Jane Doe");
//        teamLeader2.setId(1L);
//        teamLeader2.setIsActive(true);
//        teamLeader2.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader2.setOauthProviderId("42");
//        teamLeader2.setPasswordHash("Password Hash");
//        teamLeader2.setRequirePasswordChange(true);
//        teamLeader2.setRoles(new HashSet<>());
//        teamLeader2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader2.setUpdatedBy("2020-03-01");
//        teamLeader2.setUsername("janedoe");
//
//        Team team2 = new Team();
//        team2.setCode("Code");
//        team2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team2.setDeleteFlag(true);
//        team2.setEmployees(new ArrayList<>());
//        team2.setFinalInspection(finalInspection2);
//        team2.setGroup(group2);
//        team2.setId(1L);
//        team2.setName("Name");
//        team2.setTeamLeader(teamLeader2);
//        team2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team2.setUpdatedBy("2020-03-01");
//        Optional<Team> ofResult = Optional.of(team2);
//        when(teamRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        ArrayList<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(1L);
//
//        // Act
//        employeeServiceImpl.addEmployeesToTeam(1L, employeeIds);
//
//        // Assert
//        verify(teamRepository).findById(1L);
//        verify(employeeRepository).findAllById(isA(Iterable.class));
//        verify(employeeRepository).saveAll(isA(Iterable.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <ul>
//     *   <li>Given {@link EmployeeRepository} {@link EmployeeRepository#findAllById(Iterable)} return
//     *       {@link ArrayList#ArrayList()}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName(
//            "Test addEmployeesToTeam(Long, List); given EmployeeRepository findAllById(Iterable) return ArrayList()")
//    @Tag("MaintainedByDiffblue")
//    void testAddEmployeesToTeam_givenEmployeeRepositoryFindAllByIdReturnArrayList() {
//        // Arrange
//        when(employeeRepository.findAllById(Mockito.<Iterable<Long>>any()))
//                .thenReturn(new ArrayList<>());
//
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//        Optional<Team> ofResult = Optional.of(team);
//        when(teamRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        ArrayList<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(1L);
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.addEmployeesToTeam(1L, employeeIds));
//        verify(teamRepository).findById(1L);
//        verify(employeeRepository).findAllById(isA(Iterable.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <ul>
//     *   <li>Given {@link EmployeeRepository} {@link EmployeeRepository#saveAll(Iterable)} return
//     *       {@link ArrayList#ArrayList()}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName(
//            "Test addEmployeesToTeam(Long, List); given EmployeeRepository saveAll(Iterable) return ArrayList()")
//    @Tag("MaintainedByDiffblue")
//    void testAddEmployeesToTeam_givenEmployeeRepositorySaveAllReturnArrayList() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//
//        ArrayList<Employee> employeeList = new ArrayList<>();
//        employeeList.add(employee);
//        when(employeeRepository.saveAll(Mockito.<Iterable<Employee>>any()))
//                .thenReturn(new ArrayList<>());
//        when(employeeRepository.findAllById(Mockito.<Iterable<Long>>any())).thenReturn(employeeList);
//
//        User finalInspection = new User();
//        finalInspection.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        finalInspection.setDeleteFlag(true);
//        finalInspection.setEmail("jane.doe@example.org");
//        finalInspection.setEmployeeCode("Employee Code");
//        finalInspection.setFullName("Dr Jane Doe");
//        finalInspection.setId(1L);
//        finalInspection.setIsActive(true);
//        finalInspection.setOauthProvider(OAuthProvider.LOCAL);
//        finalInspection.setOauthProviderId("42");
//        finalInspection.setPasswordHash("Password Hash");
//        finalInspection.setRequirePasswordChange(true);
//        finalInspection.setRoles(new HashSet<>());
//        finalInspection.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        finalInspection.setUpdatedBy("2020-03-01");
//        finalInspection.setUsername("janedoe");
//
//        User manager = new User();
//        manager.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        manager.setDeleteFlag(true);
//        manager.setEmail("jane.doe@example.org");
//        manager.setEmployeeCode("Employee Code");
//        manager.setFullName("Dr Jane Doe");
//        manager.setId(1L);
//        manager.setIsActive(true);
//        manager.setOauthProvider(OAuthProvider.LOCAL);
//        manager.setOauthProviderId("42");
//        manager.setPasswordHash("Password Hash");
//        manager.setRequirePasswordChange(true);
//        manager.setRoles(new HashSet<>());
//        manager.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        manager.setUpdatedBy("2020-03-01");
//        manager.setUsername("janedoe");
//
//        Section section = new Section();
//        section.setCode("Code");
//        section.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        section.setDeleteFlag(true);
//        section.setGroups(new ArrayList<>());
//        section.setId(1L);
//        section.setManager(manager);
//        section.setName("Name");
//        section.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        section.setUpdatedBy("2020-03-01");
//
//        User supervisor = new User();
//        supervisor.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        supervisor.setDeleteFlag(true);
//        supervisor.setEmail("jane.doe@example.org");
//        supervisor.setEmployeeCode("Employee Code");
//        supervisor.setFullName("Dr Jane Doe");
//        supervisor.setId(1L);
//        supervisor.setIsActive(true);
//        supervisor.setOauthProvider(OAuthProvider.LOCAL);
//        supervisor.setOauthProviderId("42");
//        supervisor.setPasswordHash("Password Hash");
//        supervisor.setRequirePasswordChange(true);
//        supervisor.setRoles(new HashSet<>());
//        supervisor.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        supervisor.setUpdatedBy("2020-03-01");
//        supervisor.setUsername("janedoe");
//
//        Group group = new Group();
//        group.setCode("Code");
//        group.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        group.setDeleteFlag(true);
//        group.setId(1L);
//        group.setName("Name");
//        group.setSection(section);
//        group.setSupervisor(supervisor);
//        group.setTeams(new ArrayList<>());
//        group.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        group.setUpdatedBy("2020-03-01");
//
//        User teamLeader = new User();
//        teamLeader.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        teamLeader.setDeleteFlag(true);
//        teamLeader.setEmail("jane.doe@example.org");
//        teamLeader.setEmployeeCode("Employee Code");
//        teamLeader.setFullName("Dr Jane Doe");
//        teamLeader.setId(1L);
//        teamLeader.setIsActive(true);
//        teamLeader.setOauthProvider(OAuthProvider.LOCAL);
//        teamLeader.setOauthProviderId("42");
//        teamLeader.setPasswordHash("Password Hash");
//        teamLeader.setRequirePasswordChange(true);
//        teamLeader.setRoles(new HashSet<>());
//        teamLeader.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        teamLeader.setUpdatedBy("2020-03-01");
//        teamLeader.setUsername("janedoe");
//
//        Team team = new Team();
//        team.setCode("Code");
//        team.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        team.setDeleteFlag(true);
//        team.setEmployees(new ArrayList<>());
//        team.setFinalInspection(finalInspection);
//        team.setGroup(group);
//        team.setId(1L);
//        team.setName("Name");
//        team.setTeamLeader(teamLeader);
//        team.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        team.setUpdatedBy("2020-03-01");
//        Optional<Team> ofResult = Optional.of(team);
//        when(teamRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        ArrayList<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(1L);
//
//        // Act
//        employeeServiceImpl.addEmployeesToTeam(1L, employeeIds);
//
//        // Assert
//        verify(teamRepository).findById(1L);
//        verify(employeeRepository).findAllById(isA(Iterable.class));
//        verify(employeeRepository).saveAll(isA(Iterable.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}.
//     *
//     * <ul>
//     *   <li>Then calls {@link COWArrayList#isEmpty()}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#addEmployeesToTeam(Long, List)}
//     */
//    @Test
//    @DisplayName("Test addEmployeesToTeam(Long, List); then calls isEmpty()")
//    @Tag("MaintainedByDiffblue")
//    void testAddEmployeesToTeam_thenCallsIsEmpty() {
//        // Arrange
//        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
//        EmployeeMapperImpl employeeMapper = new EmployeeMapperImpl();
//        TeamRepository teamRepository = mock(TeamRepository.class);
//        ProcessRepository processRepository = mock(ProcessRepository.class);
//        TrainingResultDetailRepository trainingResultDetailRepository =
//                mock(TrainingResultDetailRepository.class);
//        EmployeeSkillRepository employeeSkillRepository = mock(EmployeeSkillRepository.class);
//        UserRepository userRepository = mock(UserRepository.class);
//        EmployeeSkillMapper employeeSkillMapper = mock(EmployeeSkillMapper.class);
//        EmployeeServiceImpl employeeServiceImpl =
//                new EmployeeServiceImpl(
//                        employeeRepository,
//                        employeeMapper,
//                        teamRepository,
//                        processRepository,
//                        trainingResultDetailRepository,
//                        employeeSkillRepository,
//                        userRepository,
//                        employeeSkillMapper,
//                        new ProcessMapperImpl());
//        COWArrayList<Long> employeeIds = mock(COWArrayList.class);
//        when(employeeIds.isEmpty()).thenThrow(new AppException(ErrorCode.SUCCESS));
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.addEmployeesToTeam(1L, employeeIds));
//        verify(employeeIds).isEmpty();
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#deleteEmployee(Long)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#deleteEmployee(Long)}
//     */
//    @Test
//    @DisplayName("Test deleteEmployee(Long)")
//    @Tag("MaintainedByDiffblue")
//    void testDeleteEmployee() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        Optional<Employee> ofResult = Optional.of(employee);
//        when(employeeRepository.save(Mockito.<Employee>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//        when(employeeRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.deleteEmployee(1L));
//        verify(employeeRepository).findById(1L);
//        verify(employeeRepository).save(isA(Employee.class));
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#deleteEmployee(Long)}.
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#deleteEmployee(Long)}
//     */
//    @Test
//    @DisplayName("Test deleteEmployee(Long)")
//    @Tag("MaintainedByDiffblue")
//    void testDeleteEmployee2() {
//        // Arrange
//        when(employeeRepository.findById(Mockito.<Long>any()))
//                .thenThrow(new AppException(ErrorCode.SUCCESS));
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.deleteEmployee(1L));
//        verify(employeeRepository).findById(1L);
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#deleteEmployee(Long)}.
//     *
//     * <ul>
//     *   <li>Given {@link EmployeeRepository} {@link EmployeeRepository#findById(Object)} return
//     *       empty.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#deleteEmployee(Long)}
//     */
//    @Test
//    @DisplayName("Test deleteEmployee(Long); given EmployeeRepository findById(Object) return empty")
//    @Tag("MaintainedByDiffblue")
//    void testDeleteEmployee_givenEmployeeRepositoryFindByIdReturnEmpty() {
//        // Arrange
//        Optional<Employee> emptyResult = Optional.empty();
//        when(employeeRepository.findById(Mockito.<Long>any())).thenReturn(emptyResult);
//
//        // Act and Assert
//        assertThrows(AppException.class, () -> employeeServiceImpl.deleteEmployee(1L));
//        verify(employeeRepository).findById(1L);
//    }
//
//    /**
//     * Test {@link EmployeeServiceImpl#deleteEmployee(Long)}.
//     *
//     * <ul>
//     *   <li>Given {@link EmployeeRepository} {@link EmployeeRepository#save(Object)} return {@link
//     *       Employee#Employee()}.
//     *   <li>Then calls {@link EmployeeRepository#save(Object)}.
//     * </ul>
//     *
//     * <p>Method under test: {@link EmployeeServiceImpl#deleteEmployee(Long)}
//     */
//    @Test
//    @DisplayName(
//            "Test deleteEmployee(Long); given EmployeeRepository save(Object) return Employee(); then calls save(Object)")
//    void testDeleteEmployee_givenEmployeeRepositorySaveReturnEmployee_thenCallsSave() {
//        // Arrange
//        Employee employee = new Employee();
//        employee.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee.setDeleteFlag(true);
//        employee.setEmployeeCode("Employee Code");
//        employee.setFullName("Dr Jane Doe");
//        employee.setId(1L);
//        employee.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee.setSkills(new ArrayList<>());
//        employee.setStatus(EmployeeStatus.ACTIVE);
//        employee.setTeams(new ArrayList<>());
//        employee.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee.setUpdatedBy("2020-03-01");
//        Optional<Employee> ofResult = Optional.of(employee);
//        Employee employee2 = new Employee();
//        employee2.setCreatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee2.setCreatedBy("Jan 1, 2020 8:00am GMT+0100");
//        employee2.setDeleteFlag(true);
//        employee2.setEmployeeCode("Employee Code");
//        employee2.setFullName("Dr Jane Doe");
//        employee2.setId(1L);
//        employee2.setJoinDate(LocalDate.of(1970, 1, 1));
//        employee2.setSkills(new ArrayList<>());
//        employee2.setStatus(EmployeeStatus.ACTIVE);
//        employee2.setTeams(new ArrayList<>());
//        employee2.setUpdatedAt(LocalDate.of(1970, 1, 1).atStartOfDay());
//        employee2.setUpdatedBy("2020-03-01");
//        when(employeeRepository.save(Mockito.<Employee>any())).thenReturn(employee2);
//        when(employeeRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
//
//        // Act
//        employeeServiceImpl.deleteEmployee(1L);
//
//        // Assert
//        verify(employeeRepository).findById(1L);
//        verify(employeeRepository).save(isA(Employee.class));
//    }
//}
