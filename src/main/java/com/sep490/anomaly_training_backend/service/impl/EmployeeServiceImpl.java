package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeMapper;
import com.sep490.anomaly_training_backend.mapper.TeamMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final TrainingResultDetailRepository trainingResultDetailRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        String empCode = request.getEmployeeCode() != null ? request.getEmployeeCode().trim() : null;

        if (empCode != null && employeeRepository.existsByEmployeeCode(empCode)) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_ALREADY_LINKED);
        }

        if (request.getTeamIds() != null) {
            boolean isTeamExist = teamRepository.existsById(request.getTeamIds().get(0));
            if (!isTeamExist) {
                throw new AppException(ErrorCode.TEAM_NOT_FOUND);
            }
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setEmployeeCode(empCode);

        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeNoAccountDTO> getEmployeesWithoutAccount() {
        List<Employee> employees = employeeRepository.findAllEmployeesWithoutAccount();

        return employees.stream()
                .map(e -> EmployeeNoAccountDTO.builder()
                        .id(e.getId())
                        .employeeCode(e.getEmployeeCode())
                        .fullName(e.getFullName())
                        .teamName(e.getTeams() != null ? e.getTeams().get(0).getName() : "N/A")
                        .status(e.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void removeEmployeesFromTeam(Long teamId, List<Long> employeeId) {
        List<Employee> employees = employeeRepository.findAllById(employeeId);

        for (Employee employee : employees) {
            if (employee.getTeams() != null && !employee.getTeams().isEmpty()) {
                employee.getTeams().removeIf(team -> team.getId().equals(teamId));
                employeeRepository.save(employee);
            }
        }
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.getEmployeeCode() != null && !employee.getEmployeeCode().equals(request.getEmployeeCode())) {
            if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
                throw new AppException(ErrorCode.EMPLOYEE_CODE_ALREADY_LINKED);
            }
        }

        if (request.getTeamIds() != null && !request.getTeamIds().get(0).equals(employee.getTeams().get(0).getId())) {
            boolean isTeamExist = teamRepository.existsById(request.getTeamIds().get(0));
            if (!isTeamExist) {
                throw new AppException(ErrorCode.TEAM_NOT_FOUND);
            }
        }

        employeeMapper.updateEntity(employee, request);

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employee.setDeleteFlag(true);
        employee.setStatus(EmployeeStatus.RESIGNED);

        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleteFlag())
                .map(employeeMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(e -> !e.isDeleteFlag())
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByTeam(Long teamId) {
        List<EmployeeResponse> results = employeeRepository.findByTeamsId(teamId).stream()
                .filter(e -> !e.isDeleteFlag())
                .map(employeeMapper::toDTO)
                .toList();
        for (EmployeeResponse employee : results) {
            Integer totalTraining = 0;
            Integer totalFail = 0;
            List<TrainingResultDetail> resultEmployee = trainingResultDetailRepository.findAllByEmployeeIdAndDeleteFlagFalse(employee.getId());
            for (TrainingResultDetail employeeDetail : resultEmployee) {
                if (Boolean.FALSE.equals(employeeDetail.getIsPass())) {
                    totalFail++;
                }
                totalTraining++;
            }
            employee.setTotalTraining(totalTraining);
            employee.setTotalFail(totalFail);
        }
        return results;
    }
}