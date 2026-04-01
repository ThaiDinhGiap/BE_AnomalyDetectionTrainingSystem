package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionItemsServiceImplTest {

    @Mock
    private TrainingResultDetailRepository trainingResultDetailRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @InjectMocks
    private ActionItemsServiceImpl actionItemsService;

    private Employee employee;
    private Process process;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setEmployeeCode("E001");
        employee.setFullName("John Doe");

        process = new Process();
        process.setName("Welding");

        // Set up security context with a Team Lead user
        Role teamLeaderRole = new Role();
        teamLeaderRole.setRoleCode("ROLE_TEAM_LEADER");
        teamLeaderRole.setIsActive(true);
        teamLeaderRole.setPermissions(new HashSet<>());

        User mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .employeeCode("TU001")
                .roles(Set.of(teamLeaderRole))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPendingSignatures_AsTeamLead_ShouldReturnProOutSummary() {
        TrainingResult tr = new TrainingResult();
        tr.setTitle("Tr Title");
        TrainingResultDetail detail = new TrainingResultDetail();
        detail.setId(1L);
        detail.setEmployee(employee);
        detail.setProcess(process);
        detail.setTrainingResult(tr);

        when(trainingResultDetailRepository.findPendingProOutSignatures(2L)).thenReturn(List.of(detail));

        PendingSignatureResponse result = actionItemsService.getPendingSignatures(2L);

        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getSignatureType()).isEqualTo("PRO_OUT");
        assertThat(result.getEmployeeCodes()).contains("E001");
    }

    @Test
    void getFailedTrainings_ShouldReturnSummary() {
        TrainingResult tr = new TrainingResult();
        tr.setTitle("Fail Title");
        TrainingResultDetail detail = new TrainingResultDetail();
        detail.setId(2L);
        detail.setEmployee(employee);
        detail.setProcess(process);
        detail.setTrainingResult(tr);

        when(trainingResultDetailRepository.findFailedTrainings(3L)).thenReturn(List.of(detail));

        FailedTrainingResponse result = actionItemsService.getFailedTrainings(3L);

        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getEmployeeCodes()).contains("E001");
    }

    @Test
    void getExpiringSkills_ShouldReturnSummary() {
        EmployeeSkill skill = new EmployeeSkill();
        skill.setId(10L);
        skill.setEmployee(employee);
        skill.setProcess(process);
        skill.setExpiryDate(LocalDate.now().plusDays(10));

        when(employeeSkillRepository.findExpiringSkills(eq(4L), any(LocalDate.class))).thenReturn(List.of(skill));

        ExpiringSkillResponse result = actionItemsService.getExpiringSkills(4L);

        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getDaysThreshold()).isEqualTo(15);
        assertThat(result.getEmployeeCodes()).contains("E001");
    }
}
