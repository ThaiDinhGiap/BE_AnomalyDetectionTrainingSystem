package com.sep490.anomaly_training_backend.util;

import com.sep490.anomaly_training_backend.repository.DefectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility component để generate defect code
 * Format: DF + 4 digits (DF0001, DF0002, ..., DF9999)
 */
@Component
@RequiredArgsConstructor
public class DefectCodeGenerator {
    private final DefectRepository defectRepository;

    /**
     * Generate defect code tự động dựa trên số thứ tự tiếp theo
     * Ví dụ: DF0001, DF0002, ..., DF0013
     * @return defect code theo format DF + 4 digits
     */
    public String generateDefectCode() {
        long nextSequence = getNextSequence();
        return String.format("DF%04d", nextSequence);
    }

    /**
     * Lấy số thứ tự tiếp theo cho defect code
     * @return số thứ tự tiếp theo (max hiện tại + 1, minimum là 1)
     */
    private long getNextSequence() {
        return defectRepository.findMaxDefectCodeSequence()
                .map(maxSeq -> maxSeq + 1)
                .orElse(1L);
    }
}

