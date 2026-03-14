package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.AttachmentDeleteOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttachmentDeleteOutboxRepository extends JpaRepository<AttachmentDeleteOutbox, Long> {
    // Lấy các tác vụ đang chờ và đã đến thời gian thực thi
    List<AttachmentDeleteOutbox> findByStatusAndNextRunAtLessThanEqual(String status, LocalDateTime currentTime);
}
