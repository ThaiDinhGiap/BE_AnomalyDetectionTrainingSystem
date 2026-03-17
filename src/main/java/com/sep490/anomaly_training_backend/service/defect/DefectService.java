package com.sep490.anomaly_training_backend.service.defect;

import com.sep490.anomaly_training_backend.dto.response.DefectCoverageResponse;
import com.sep490.anomaly_training_backend.dto.response.DefectResponse;
import com.sep490.anomaly_training_backend.model.User;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DefectService {
    List<DefectResponse> getDefectBySupervisor(Long userId);

    List<DefectResponse> getDefectByProductLine(Long productLineId);

    List<DefectResponse> getDefectByProcess(Long processId);

    DefectResponse getDefectById(Long id);

    Boolean checkExistDefectDescription(String defectDescription);

    List<DefectResponse> importDefect(User currentUser, MultipartFile file) throws BadRequestException;

    List<DefectCoverageResponse> getCoverageInProductLine(Long productLineId);
}
