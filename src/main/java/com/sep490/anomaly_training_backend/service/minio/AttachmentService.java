package com.sep490.anomaly_training_backend.service.minio;

import com.sep490.anomaly_training_backend.model.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    /**
     * Uploads a file as an attachment and links it to an entity.
     *
     * @param file the file to upload
     * @param entityType the type of the entity (e.g., "DEFECT")
     * @param entityId the ID of the entity
     * @param createdBy the user who uploaded the file
     * @return the saved Attachment metadata
     */
    Attachment uploadAttachment(MultipartFile file, String entityType, Long entityId, String createdBy);
    List<Attachment> uploadAttachments(List<MultipartFile> files, String entityType, Long entityId, String createdBy);
    /**
     * Retrieves all attachments for a specific entity.
     *
     * @param entityType the type of the entity
     * @param entityId the ID of the entity
     * @return a list of attachments
     */
    List<Attachment> getAttachmentsByEntity(String entityType, Long entityId);

    /**
     * Marks an attachment for deletion using the outbox pattern.
     * The actual file deletion from MinIO will be handled by a background job.
     *
     * @param attachmentId the ID of the attachment to delete
     */
    void deleteAttachment(Long attachmentId);
}
