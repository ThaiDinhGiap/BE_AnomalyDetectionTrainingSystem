package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.ExpiringSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.FailedTrainingResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingSignatureResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.TrainingResult;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

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
    }

    @Test
    void getPendingSignatures_ShouldReturnList() {
        TrainingResult tr = new TrainingResult();
        tr.setTitle("Tr Title");
        TrainingResultDetail detail = new TrainingResultDetail();
        detail.setId(1L);
        detail.setEmployee(employee);
        detail.setProcess(process);
        detail.setTrainingResult(tr);

        when(trainingResultDetailRepository.findPendingSignatures(2L)).thenReturn(List.of(detail));

        List<PendingSignatureResponse> result = actionItemsService.getPendingSignatures(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeName()).isEqualTo("John Doe");
    }

    @Test
    void getFailedTrainings_ShouldReturnList() {
        TrainingResult tr = new TrainingResult();
        tr.setTitle("Fail Title");
        TrainingResultDetail detail = new TrainingResultDetail();
        detail.setId(2L);
        detail.setEmployee(employee);
        detail.setProcess(process);
        detail.setTrainingResult(tr);

        when(trainingResultDetailRepository.findFailedTrainings(3L)).thenReturn(List.of(detail));

        List<FailedTrainingResponse> result = actionItemsService.getFailedTrainings(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeCode()).isEqualTo("E001");
    }

    @Test
    void getExpiringSkills_ShouldReturnList() {
        EmployeeSkill skill = new EmployeeSkill();
        skill.setId(10L);
        skill.setEmployee(employee);
        skill.setProcess(process);
        skill.setExpiryDate(LocalDate.now().plusDays(10));

        when(employeeSkillRepository.findExpiringSkills(eq(4L), any(LocalDate.class))).thenReturn(List.of(skill));

        List<ExpiringSkillResponse> result = actionItemsService.getExpiringSkills(4L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillId()).isEqualTo(10L);
    }
}
