package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.mapper.TeamMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.mapper.EmployeeMapper;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
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

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // 1. Chuẩn hóa dữ liệu (Tránh trường hợp người dùng nhập toàn dấu cách)
        String empCode = request.getEmployeeCode() != null ? request.getEmployeeCode().trim() : null;

        // 2. Kiểm tra nghiệp vụ: Trùng lặp mã nhân viên
        if (empCode != null && employeeRepository.existsByEmployeeCode(empCode)) {
            // Lời khuyên: Nên ném Custom Exception (ví dụ: DuplicateResourceException)
            // để GlobalExceptionHandler trả về mã HTTP 409 Conflict thay vì 500
            throw new RuntimeException("Mã nhân viên '" + empCode + "' đã tồn tại trong hệ thống.");
        }

        // 3. [QUAN TRỌNG] Kiểm tra Khóa ngoại: Team/Phòng ban có thực sự tồn tại không?
        if (request.getTeamId() != null) {
            boolean isTeamExist = teamRepository.existsById(request.getTeamId());
            if (!isTeamExist) {
                throw new RuntimeException("Không tìm thấy nhóm/phòng ban với ID: " + request.getTeamId());
            }
        }

        // 4. Kiểm tra thêm (Nếu bảng Employee yêu cầu Email/Phone là duy nhất)
        // if (request.getEmail() != null && employeeRepository.existsByEmail(request.getEmail())) { ... }

        // 5. Map dữ liệu sang Entity
        Employee employee = employeeMapper.toEntity(request);

        // Ghi đè lại mã nhân viên đã được loại bỏ khoảng trắng thừa
        employee.setEmployeeCode(empCode);

        // 6. Gán giá trị mặc định an toàn
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        // 7. Lưu và trả về
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
                        .teamName(e.getTeam() != null ? e.getTeam().getName(): "N/A")
                        .status(e.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        // 1. Nếu có thay đổi mã nhân viên, cần check trùng lặp
        if (request.getEmployeeCode() != null && !employee.getEmployeeCode().equals(request.getEmployeeCode())) {
            if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
                throw new RuntimeException("Mã nhân viên '" + request.getEmployeeCode() + "' đã tồn tại");
            }
        }

        // 2. [QUAN TRỌNG] Kiểm tra Team có thực sự tồn tại không (nếu có thay đổi)
        if (request.getTeamId() != null && !request.getTeamId().equals(employee.getTeam().getId())) {
            boolean isTeamExist = teamRepository.existsById(request.getTeamId());
            if (!isTeamExist) {
                throw new RuntimeException("Không tìm thấy nhóm/phòng ban với ID: " + request.getTeamId());
            }
        }

        // 3. Map dữ liệu cập nhật
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