package com.sep490.anomaly_training_backend.service.minio.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.model.AttachmentDeleteOutbox;
import com.sep490.anomaly_training_backend.repository.AttachmentDeleteOutboxRepository;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.IStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final IStorageService storageService;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentDeleteOutboxRepository outboxRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${app.attachment.max-files-per-request:5}")
    private int maxFilesPerRequest;

    @Override
    @Transactional
    public Attachment uploadAttachment(MultipartFile file, String entityType, Long entityId, String createdBy) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
        String objectKey = generateObjectKey(entityType, file.getOriginalFilename());
        storageService.uploadFile(file, objectKey);

        try {
            Attachment attachment = new Attachment();
            attachment.setEntityType(entityType);
            attachment.setEntityId(entityId);
            attachment.setBucket(bucketName);
            attachment.setObjectKey(objectKey);
            attachment.setOriginalFilename(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setSizeBytes(file.getSize());
            attachment.setStatus("ACTIVE");
            attachment.setCreatedBy(createdBy);
            attachment.setUpdatedBy(createdBy);
            return attachmentRepository.save(attachment);
        } catch (Exception e) {
            log.error("Failed to save attachment to DB, rolling back MinIO file: {}", objectKey, e);
            try {
                storageService.deleteFile(objectKey);
            } catch (Exception minioEx) {
                log.error("Failed to rollback MinIO file!", minioEx);
            }
            throw new AppException(ErrorCode.DATABASE_SAVE_ERROR, e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<Attachment> uploadAttachments(List<MultipartFile> files, String entityType, Long entityId, String createdBy) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.NO_FILE_SELECTED);
        }
        if (files.size() > maxFilesPerRequest) {
            throw new AppException(ErrorCode.MAX_FILES_EXCEEDED, "You can only upload a maximum of " + maxFilesPerRequest + " files at a time.");
        }

        List<Attachment> savedAttachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Attachment attachment = processSingleFile(file, entityType, entityId, createdBy);
                savedAttachments.add(attachment);
            }
        }
        return savedAttachments;
    }

    private Attachment processSingleFile(MultipartFile file, String entityType, Long entityId, String createdBy) {
        String objectKey = generateObjectKey(entityType, file.getOriginalFilename());
        storageService.uploadFile(file, objectKey);

        try {
            Attachment attachment = new Attachment();
            attachment.setEntityType(entityType);
            attachment.setEntityId(entityId);
            attachment.setBucket(bucketName);
            attachment.setObjectKey(objectKey);
            attachment.setOriginalFilename(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setSizeBytes(file.getSize());
            attachment.setStatus("ACTIVE");
            attachment.setCreatedBy(createdBy);
            attachment.setUpdatedBy(createdBy);
            return attachmentRepository.save(attachment);
        } catch (Exception e) {
            log.error("Error saving attachment to DB, rolling back MinIO file: {}", objectKey, e);
            try {
                storageService.deleteFile(objectKey);
            } catch (Exception minioEx) {
                log.error("Failed to rollback MinIO file!", minioEx);
            }
            throw new AppException(ErrorCode.DATABASE_SAVE_ERROR, e.getMessage());
        }
    }

    @Override
    public List<Attachment> getAttachmentsByEntity(String entityType, Long entityId) {
        List<Attachment> attachments = attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        for (Attachment attachment : attachments) {
            if (!"DELETING".equals(attachment.getStatus())) {
                String url = storageService.getFileUrl(attachment.getObjectKey());
                attachment.setUrl(url);
            }
        }
        return attachments;
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        String updatedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTACHMENT_NOT_FOUND));

        attachment.setStatus("DELETING");
        attachment.setUpdatedBy(updatedBy);
        attachmentRepository.save(attachment);

        AttachmentDeleteOutbox outboxItem = new AttachmentDeleteOutbox(
                attachment.getId(),
                attachment.getBucket(),
                attachment.getObjectKey()
        );
        outboxRepository.save(outboxItem);
    }

    private String generateObjectKey(String entityType, String originalFilename) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s/%s/%s-%s", entityType.toLowerCase(), datePart, randomPart, originalFilename);
    }
}