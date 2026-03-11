package com.sep490.anomaly_training_backend.util;

import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility component để generate training code
 * Format: TS + 6 digits (TS000001, TS000002, ..., TS999999)
 */
@Component
@RequiredArgsConstructor
public class TrainingCodeGenerator {
    private final TrainingSampleRepository trainingSampleRepository;

    /**
     * Generate training code tự động dựa trên số thứ tự tiếp theo
     * Ví dụ: TS000001, TS000002, ..., TS000013
     * @return training code theo format TS + 6 digits
     */
    public String generateTrainingCode() {
        long nextSequence = getNextSequence();
        return String.format("TS%06d", nextSequence);
    }

    /**
     * Lấy số thứ tự tiếp theo cho training code
     * @return số thứ tự tiếp theo (max hiện tại + 1, minimum là 1)
     */
    private long getNextSequence() {
        return trainingSampleRepository.findMaxTrainingCodeSequence()
                .map(maxSeq -> maxSeq + 1)
                .orElse(1L);
    }
}

