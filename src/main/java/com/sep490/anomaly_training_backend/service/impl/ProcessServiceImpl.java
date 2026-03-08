package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import com.sep490.anomaly_training_backend.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;
    private final ProcessMapper processMapper;

    @Override
    @Transactional
    public ProcessResponse createProcess(ProcessRequest request) {
        if (processRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã quy trình '" + request.getCode() + "' đã tồn tại");
        }

        Process entity = processMapper.toEntity(request);
        return processMapper.toDTO(processRepository.save(entity));
    }

    @Override
    @Transactional
    public ProcessResponse updateProcessByAdmin(Long id, ProcessRequest request) {
        Process entity = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found id: " + id));

        // Check trùng code nếu có thay đổi
        if (!entity.getCode().equals(request.getCode())
                && processRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã quy trình mới đã tồn tại");
        }
        entity.setDescription(request.getDescription());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        return processMapper.toDTO(processRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteProcess(Long id) {
        Process entity = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found id: " + id));

        // CẬP NHẬT: Soft Delete
        entity.setDeleteFlag(true);
        processRepository.save(entity);
    }

    @Override
    public ProcessResponse getProcessById(Long id) {
        return processRepository.findById(id)
                .filter(p -> !p.isDeleteFlag()) // Chỉ lấy bản ghi chưa xóa
                .map(processMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Process not found or deleted. ID: " + id));
    }

    @Override
    public List<ProcessResponse> getAllProcesses() {
        return processRepository.findAll().stream()
                .filter(p -> !p.isDeleteFlag()) // Filter đã xóa
                .map(processMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessResponse> getProcessesByProductLineId(Long productLineId) {
        return processRepository.findByProductLineId(productLineId).stream()
                .filter(p -> !p.isDeleteFlag()) // Filter đã xóa
                .map(processMapper::toDTO)
                .collect(Collectors.toList());
    }
}