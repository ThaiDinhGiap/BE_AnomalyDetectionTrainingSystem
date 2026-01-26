package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    boolean existsByCode(String code);

    List<Process> findByGroupId(Long groupId);

    /**
     * Nếu Công đoạn gắn chặt với từng Group (Mỗi dây chuyền có quy trình riêng)
     */
    List<Process> findByGroup_IdAndDeleteFlagFalse(Long groupId);

}

