package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.ProcessMapper;
import com.sep490.anomaly_training_backend.model.Process;
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
            throw new AppException(ErrorCode.PROCESS_CODE_ALREADY_EXISTS);
        }

        Process entity = processMapper.toEntity(request);
        return processMapper.toDTO(processRepository.save(entity));
    }

    @Override
    @Transactional
    public ProcessResponse updateProcessByAdmin(Long id, ProcessRequest request) {
        Process entity = processRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        if (request.getCode() != null && !entity.getCode().equals(request.getCode())
                && processRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.PROCESS_CODE_ALREADY_EXISTS);
        }

        processMapper.updateEntity(entity, request);

        return processMapper.toDTO(processRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteProcess(Long id) {
        Process entity = processRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));

        entity.setDeleteFlag(true);
        processRepository.save(entity);
    }

    @Override
    public ProcessResponse getProcessById(Long id) {
        return processRepository.findById(id)
                .filter(p -> !p.isDeleteFlag())
                .map(processMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.PROCESS_NOT_FOUND));
    }

    @Override
    public List<ProcessResponse> getAllProcesses() {
        return processRepository.findAll().stream()
                .filter(p -> !p.isDeleteFlag())
                .map(processMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessResponse> getProcessesByProductLineId(Long productLineId) {
        return processRepository.findByProductLineId(productLineId).stream()
                .filter(p -> !p.isDeleteFlag())
                .map(processMapper::toDTO)
                .collect(Collectors.toList());
    }
}