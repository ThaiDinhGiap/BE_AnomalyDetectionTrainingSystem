package com.sep490.anomaly_training_backend.service.minio;

import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.AttachmentDeleteOutbox;
import com.sep490.anomaly_training_backend.repository.AttachmentDeleteOutboxRepository;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
import com.sep490.anomaly_training_backend.service.minio.IStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentCleanupJob {

    private final AttachmentDeleteOutboxRepository outboxRepository;
    private final AttachmentRepository attachmentRepository;
    private final IStorageService storageService;

    // Chạy ngầm mỗi 50 phút (3,000,000 milliseconds)
    @Scheduled(fixedDelayString = "${app.cron.outbox-cleanup:30000}")
    public void processDeleteOutbox() {
        log.info("Starting Outbox Cleanup Job...");

        // Lấy các bản ghi PENDING và đến hạn chạy (next_run_at <= hiện tại)
        List<AttachmentDeleteOutbox> pendingItems = outboxRepository
                .findByStatusAndNextRunAtLessThanEqual("PENDING", LocalDateTime.now());

        for (AttachmentDeleteOutbox item : pendingItems) {
            try {
                // 1. Xóa file trên MinIO
                storageService.deleteFile(item.getObjectKey());

                // 2. Cập nhật Outbox thành DONE
                item.setStatus("DONE");
                outboxRepository.save(item);

                // 3. CẬP NHẬT BẢNG ATTACHMENTS
                Optional<Attachment> attachmentOpt = attachmentRepository.findById(item.getAttachmentId());
                if (attachmentOpt.isPresent()) {
                    Attachment attachment = attachmentOpt.get();
                    attachment.setStatus("DELETED");
                    attachment.setDeleteFlag(true);
                    attachmentRepository.save(attachment);
                    log.info("Cập nhật DB: Đã xóa mềm attachment ID {}", attachment.getId());
                }

            } catch (Exception e) {
                // 3. Xử lý lỗi (Cơ chế Retry / Exponential Backoff)
                log.error("Failed to delete file from MinIO: {}", item.getObjectKey(), e);

                item.setAttempts(item.getAttempts() + 1);
                item.setLastError(e.getMessage());

                if (item.getAttempts() >= 5) {
                    item.setStatus("FAILED"); // Bỏ cuộc sau 5 lần thử
                } else {
                    item.setNextRunAt(LocalDateTime.now().plusMinutes(15L * item.getAttempts()));
                }
            }
            outboxRepository.save(item);
        }
    }
}