package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void deleteEmployee_ShouldSoftDelete() {
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setDeleteFlag(false);

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(10L);

        assertThat(employee.isDeleteFlag()).isTrue();
        verify(employeeRepository).save(employee);
    }

    @Test
    void deleteEmployee_WhenNotFound_ShouldThrow() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(10L))
                .isInstanceOf(AppException.class);
    }

}
