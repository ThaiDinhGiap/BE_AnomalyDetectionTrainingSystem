package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.EmployeeSkillRequest;
import com.sep490.anomaly_training_backend.dto.response.EmployeeSkillResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.EmployeeSkillMapper;
import com.sep490.anomaly_training_backend.model.Employee;
import com.sep490.anomaly_training_backend.model.EmployeeSkill;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.EmployeeSkillRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.service.EmployeeSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeSkillServiceImpl implements EmployeeSkillService {
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
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
}