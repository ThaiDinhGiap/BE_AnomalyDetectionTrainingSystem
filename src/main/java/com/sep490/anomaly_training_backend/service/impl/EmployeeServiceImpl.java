package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeNoAccountDTO;
import com.sep490.anomaly_training_backend.dto.response.EmployeeResponse;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.enums.EmployeeStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeMapper;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.EmployeeService;
import com.sep490.anomaly_training_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sep490.anomaly_training_backend.util.SecurityUtils.hasPermission;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final TeamRepository teamRepository;
    private final ProcessRepository processRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final UserRepository userRepository;

    private final EmployeeSkillMapper employeeSkillMapper;
    private final ProcessMapper processMapper;

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
                .collect(toList());
    }

    @Override
    @Transactional
    public void removeEmployeesFromTeam(Long teamId, List<Long> employeeId) {
        if (employeeId == null || employeeId.isEmpty()) return;

        if (!teamRepository.existsById(teamId)) {
            throw new AppException(ErrorCode.TEAM_NOT_FOUND);
        }

        employeeRepository.removeEmployeesFromTeam(teamId, employeeId);
    }

    @Override
    public ProcessResponse getEmployeesByProcess(Long processId) {
        ProcessResponse processResponse = processRepository.findById(processId).map(processMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        processResponse.setEmployeeSkills(employeeSkillRepository.findByProcessIdAndDeleteFlagFalse(processId).stream()
                .map(employeeSkillMapper::toDto).toList());

        return processResponse;
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
                .collect(toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesUnderManagement(User currentUser) {
        Set<Long> teamIds = new HashSet<>();

        if (currentUser != null) {
            if (SecurityUtils.hasPermission(currentUser, "section.manage")) {
                teamIds.addAll(teamRepository.findAllBySectionManagerId(currentUser.getId())
                        .stream().map(Team::getId).toList());
            }
            if (hasPermission(currentUser, "group.manage")) {
                teamIds.addAll(teamRepository.findAllByGroupSupervisorId(currentUser.getId())
                        .stream().map(Team::getId).toList());
            }
            if (hasPermission(currentUser, "team.manage")) {
                teamIds.addAll(teamRepository.findAllByTeamLeaderId(currentUser.getId())
                        .stream().map(Team::getId).toList());
            }
        }

        if (teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Employee> employees = employeeRepository.findAllByTeamIdIn(new ArrayList<>(teamIds));

        return employees.stream()
                .filter(e -> !e.isDeleteFlag())
                .map(this::enrichEmployeeData)
                .toList();
    }

    private EmployeeResponse enrichEmployeeData(Employee employee) {
        EmployeeResponse dto = employeeMapper.toDTO(employee);

        if (employee.getTeams() != null && !employee.getTeams().isEmpty()) {
            String teamNames = employee.getTeams().stream()
                    .map(Team::getName).collect(Collectors.joining(", "));
            dto.setTeamName(teamNames);

            Team firstTeam = employee.getTeams().get(0);
            dto.setGroupName(firstTeam.getGroup() != null ? firstTeam.getGroup().getName() : "N/A");
            dto.setSectionName(firstTeam.getGroup() != null && firstTeam.getGroup().getSection() != null
                    ? firstTeam.getGroup().getSection().getName() : "N/A");
        }

        userRepository.findByEmployeeCodeAndDeleteFlagFalse(employee.getEmployeeCode())
                .ifPresent(user -> dto.setRoles(user.getRoles().stream()
                        .map(Role::getDisplayName).toList()));

        List<TrainingResultDetail> results = trainingResultDetailRepository
                .findAllByEmployeeIdAndDeleteFlagFalse(employee.getId());

        int totalTraining = results.size();
        long totalFail = results.stream().filter(r -> Boolean.FALSE.equals(r.getIsPass())).count();

        dto.setTotalTraining(totalTraining);
        dto.setTotalFail((int) totalFail);

        return dto;
    }
}