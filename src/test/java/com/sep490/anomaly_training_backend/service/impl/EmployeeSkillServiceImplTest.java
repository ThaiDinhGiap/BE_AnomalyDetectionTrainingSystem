package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeSkillServiceImplTest {

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @InjectMocks
    private EmployeeSkillServiceImpl employeeSkillService;

    @Test
    void deleteEmployeeSkill_ShouldSoftDelete() {
        EmployeeSkill skill = new EmployeeSkill();
        skill.setId(10L);
        skill.setDeleteFlag(false);

        when(employeeSkillRepository.findById(10L)).thenReturn(Optional.of(skill));

        employeeSkillService.deleteEmployeeSkill(10L);

        assertThat(skill.isDeleteFlag()).isTrue();
        verify(employeeSkillRepository).save(skill);
    }

    @Test
    void deleteEmployeeSkill_WhenNotFound_ShouldThrow() {
        when(employeeSkillRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeSkillService.deleteEmployeeSkill(10L))
                .isInstanceOf(AppException.class);
    }
}
