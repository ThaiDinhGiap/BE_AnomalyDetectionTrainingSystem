package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.EmployeeSkillsDto;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.ProcessCompletionDto;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillMatrixResponse;
import com.sep490.anomaly_training_backend.dto.response.skill_matrix.SkillStatusDto;
import com.sep490.anomaly_training_backend.enums.EmployeeSkillStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;
    private final EmployeeSkillMapper employeeSkillMapper;


    @Override
    public EmployeeSkillResponse createEmployeeSkill(EmployeeSkillRequest request) {

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        EmployeeSkill entity = EmployeeSkill.builder()
                .employee(employee)
                .process(process)
                .certifiedDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(2))
                .build();

        EmployeeSkill saved = employeeSkillRepository.save(entity);

        return employeeSkillMapper.toDto(saved);
    }

    @Override
    public EmployeeSkillResponse updateEmployeeSkillByTeamLead(Long id, EmployeeSkillRequest request) {
        EmployeeSkill skill = employeeSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        skill.setEmployee(employee);
        skill.setProcess(process);
        skill.setStatus(request.getStatus());
        return employeeSkillMapper.toDto(employeeSkillRepository.save(skill));
    }

    @Override
    public void deleteEmployeeSkill(Long id) {
        EmployeeSkill skill = employeeSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SKILL_NOT_FOUND));
        skill.setDeleteFlag(true);
        employeeSkillRepository.save(skill);
    }

    @Override
    public SkillMatrixResponse getSkillMatrix(Long teamId, Long lineId, List<Long> employeeIds, List<Long> processIds) {
        // 1. Get processes for the header, filtering if processIds are provided
        List<Process> processModels = (CollectionUtils.isEmpty(processIds))
                ? processRepository.findByProductLineIdAndDeleteFlagFalse(lineId)
                : processRepository.findAllById(processIds);

        // 2. Get employees for the rows, filtering if employeeIds are provided
        List<Employee> employees = (CollectionUtils.isEmpty(employeeIds))
                ? employeeRepository.findByTeamIdAndDeleteFlagFalse(teamId)
                : employeeRepository.findAllById(employeeIds);
        List<Long> finalEmployeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());

        // 3. Get all relevant skills and training details in bulk
        Map<Long, List<EmployeeSkill>> skillsByEmployee = employeeSkillRepository.findByEmployeeIdIn(finalEmployeeIds)
                .stream().collect(Collectors.groupingBy(skill -> skill.getEmployee().getId()));

        List<Long> pIds = processModels.stream().map(Process::getId).collect(Collectors.toList());
        Map<Long, Long> completedCountByProcess = trainingResultDetailRepository.countCompletedByProcessIds(pIds)
                .stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> totalCountByProcess = trainingResultDetailRepository.countTotalByProcessIds(pIds)
                .stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        // 4. Build Process DTOs with completion rate
        List<ProcessCompletionDto> processDtos = processModels.stream().map(p -> {
            ProcessCompletionDto dto = new ProcessCompletionDto(p.getId(), p.getCode(), p.getName());
            long completed = completedCountByProcess.getOrDefault(p.getId(), 0L);
            long total = totalCountByProcess.getOrDefault(p.getId(), 0L);
            dto.setCompletionRate(completed + "/" + total);
            return dto;
        }).collect(Collectors.toList());


        // 5. Build the matrix data
        List<EmployeeSkillsDto> employeeSkillsDtos = employees.stream().map(employee -> {
            EmployeeSkillsDto dto = new EmployeeSkillsDto();
            dto.setEmployeeId(employee.getId());
            dto.setEmployeeName(employee.getFullName());
            dto.setEmployeeCode(employee.getEmployeeCode());

            List<EmployeeSkill> employeeSkillsList = skillsByEmployee.getOrDefault(employee.getId(), Collections.emptyList());
            Map<Long, SkillStatusDto> skillsMap = employeeSkillsList.stream()
                    .collect(Collectors.toMap(
                            skill -> skill.getProcess().getId(),
                            this::mapToSkillStatusDto
                    ));

            // Fill in missing skills with "NONE" status
            processModels.forEach(process -> {
                skillsMap.putIfAbsent(process.getId(), SkillStatusDto.builder().status("NONE").build());
            });

            dto.setSkills(skillsMap);
            return dto;
        }).collect(Collectors.toList());

        SkillMatrixResponse response = new SkillMatrixResponse();
        response.setProcesses(processDtos);
        response.setEmployeeSkills(employeeSkillsDtos);
        return response;
    }

    private SkillStatusDto mapToSkillStatusDto(EmployeeSkill skill) {
        String status;
        LocalDate expiryDate = skill.getExpiryDate();
        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysFromNow = today.plusDays(90);

        switch (skill.getStatus()) {
            case VALID:
                if (expiryDate != null) {
                    if (expiryDate.isBefore(today)) {
                        status = "EXPIRED";
                    } else if (expiryDate.isBefore(ninetyDaysFromNow)) {
                        status = "WARNING";
                    } else {
                        status = "PASS";
                    }
                } else {
                    status = "PASS";
                }
                break;
            case PENDING_REVIEW:
                status = "PENDING";
                break;
            case REVOKED:
                status = "REVOKED";
                break;
            default:
                status = "NONE";
                break;
        }

        return SkillStatusDto.builder()
                .status(status)
                .expiryDate(expiryDate)
                .build();
    }
}
