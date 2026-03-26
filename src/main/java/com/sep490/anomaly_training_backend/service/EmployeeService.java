package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.model.User;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id); // Soft delete

    EmployeeResponse getEmployeeById(Long id);

    List<EmployeeResponse> getAllEmployees();

    List<EmployeeResponse> getEmployeesUnderManagement(User currentUser);

    List<EmployeeNoAccountDTO> getEmployeesWithoutAccount();

    void removeEmployeesFromTeam(Long teamId, List<Long> employeeId);

    void addEmployeesToTeam(Long teamId, List<Long> employeeIds);

    ProcessResponse getEmployeesByProcess(Long processId);
}