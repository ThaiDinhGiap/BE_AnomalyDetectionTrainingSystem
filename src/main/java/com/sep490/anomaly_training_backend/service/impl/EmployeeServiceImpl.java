package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.mapper.EmployeeMapper;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
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

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Validate mã nhân viên trùng lặp
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new RuntimeException("Mã nhân viên '" + request.getEmployeeCode() + "' đã tồn tại");
        }

        Employee employee = employeeMapper.toEntity(request);

        // Nếu request chưa có status, đảm bảo set mặc định (dù Entity có Builder.Default, kiểm tra cho chắc)
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found id: " + id));

        // Nếu có thay đổi mã nhân viên, cần check trùng lặp (trừ chính nó)
        if (!employee.getEmployeeCode().equals(request.getEmployeeCode())
                && employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new RuntimeException("Mã nhân viên mới đã tồn tại");
        }

        employeeMapper.updateEntity(employee, request);
        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found id: " + id));

        // Soft Delete
        employee.setDeleteFlag(true);
        // Có thể cân nhắc chuyển status sang RESIGNED luôn nếu muốn
        employee.setStatus(EmployeeStatus.RESIGNED);

        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> !e.isDeleteFlag())
                .map(employeeMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Employee not found or deleted"));
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
        return employeeRepository.findByTeamId(teamId).stream()
                .filter(e -> !e.isDeleteFlag())
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }
}