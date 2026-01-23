package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProcessQualificationRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessQualificationResponse;
import com.sep490.anomaly_training_backend.model.ProcessQualification;
import com.sep490.anomaly_training_backend.mapper.ProcessQualificationMapper;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.repository.ProcessQualificationRepository;
import com.sep490.anomaly_training_backend.service.ProcessQualificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessQualificationServiceImpl implements ProcessQualificationService {

    private final ProcessQualificationRepository qualificationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProcessRepository processRepository;
    private final ProcessQualificationMapper qualificationMapper;

    @Override
    @Transactional
    public ProcessQualificationResponse createQualification(ProcessQualificationRequest request) {
        validateRequest(request);

        // Check Unique
        if (qualificationRepository.existsByEmployeeIdAndProcessId(request.getEmployeeId(), request.getProcessId())) {
            throw new RuntimeException("Nhân viên này đã có chứng chỉ cho quy trình này rồi.");
        }

        ProcessQualification entity = qualificationMapper.toEntity(request);
        if (entity.getIsQualified() == null) entity.setIsQualified(true);

        return qualificationMapper.toDTO(qualificationRepository.save(entity));
    }

    @Override
    @Transactional
    public ProcessQualificationResponse updateQualification(Long id, ProcessQualificationRequest request) {
        ProcessQualification entity = qualificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Qualification not found id: " + id));

        // Nếu user đổi nhân viên hoặc quy trình -> Check Unique lại
        boolean isChanged = !entity.getEmployee().getId().equals(request.getEmployeeId())
                || !entity.getProcess().getId().equals(request.getProcessId());

        if (isChanged) {
            validateRequest(request); // Check ID tồn tại
            if (qualificationRepository.existsByEmployeeIdAndProcessId(request.getEmployeeId(), request.getProcessId())) {
                throw new RuntimeException("Cặp Nhân viên - Quy trình này đã tồn tại.");
            }
        } else {
            // Vẫn phải check date logic
            if (request.getExpiryDate() != null && request.getCertifiedDate().isAfter(request.getExpiryDate())) {
                throw new RuntimeException("Ngày hết hạn phải sau ngày cấp.");
            }
        }

        qualificationMapper.updateEntity(entity, request);
        return qualificationMapper.toDTO(qualificationRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteQualification(Long id) {
        ProcessQualification entity = qualificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Qualification not found id: " + id));

        // Soft Delete
        entity.setDeleteFlag(true);
        qualificationRepository.save(entity);
    }

    @Override
    public ProcessQualificationResponse getQualificationById(Long id) {
        return qualificationRepository.findById(id)
                .filter(q -> !q.isDeleteFlag())
                .map(qualificationMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Not found or deleted"));
    }

    @Override
    public List<ProcessQualificationResponse> getAllQualifications() {
        return qualificationRepository.findAll().stream()
                .filter(q -> !q.isDeleteFlag())
                .map(qualificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessQualificationResponse> getByEmployee(Long employeeId) {
        return qualificationRepository.findByEmployeeId(employeeId).stream()
                .filter(q -> !q.isDeleteFlag())
                .map(qualificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessQualificationResponse> getByProcess(Long processId) {
        return qualificationRepository.findByProcessId(processId).stream()
                .filter(q -> !q.isDeleteFlag())
                .map(qualificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    // --- Helper Validation ---
    private void validateRequest(ProcessQualificationRequest request) {
        // 1. Check ID tồn tại (Referential Integrity Check thủ công)
        if (!employeeRepository.existsById(request.getEmployeeId())) {
            throw new RuntimeException("Employee ID không tồn tại: " + request.getEmployeeId());
        }
        if (!processRepository.existsById(request.getProcessId())) {
            throw new RuntimeException("Process ID không tồn tại: " + request.getProcessId());
        }

        // 2. Check Logic ngày tháng
        if (request.getExpiryDate() != null && request.getCertifiedDate().isAfter(request.getExpiryDate())) {
            throw new RuntimeException("Ngày hết hạn phải sau ngày cấp.");
        }
    }
}