package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.ProcessRequest;
import com.sep490.anomaly_training_backend.dto.response.ProcessResponse;

import java.util.List;

public interface ProcessService {
    ProcessResponse createProcess(ProcessRequest request);

//    ProcessResponse updateProcess(Long id, ProcessRequest request);

    void deleteProcess(Long id);

//    ProcessResponse getProcessById(Long id);
//
//    List<ProcessResponse> getAllProcesses();

    List<ProcessResponse> getProcessesByProductLineId(Long groupId);

    ProcessResponse updateProcessByAdmin(Long id, ProcessRequest request);

}