package com.sep490.anomaly_training_backend.service.minio.impl;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.service.minio.ImportImageHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;


/**
 * Implementation của ImportImageHandlerService
 * 
 * Xử lý image extraction từ Excel rows và upload lên MinIO
 * Cung cấp centralized logic cho cả Defect và TrainingSample import
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportImageHandlerServiceImpl implements ImportImageHandlerService {

    private final AttachmentService attachmentService;

    /**
     * Xử lý images từ một row Excel
     * <p>
     * Flow:
     * 1. Extract images từ row (nếu row là XSSFSheet)
     * 2. Validate images (size, type, etc)
     * 3. Upload từng image lên MinIO via AttachmentService
     * 4. Return list Attachment records
     */
    @Override
    @Transactional
    public void handleRowImages(
            ImageData imageData,
            String entityType,
            Long entityId,
            String createdBy) {

        Attachment attachment = new Attachment();

        if (imageData == null) {
            log.debug("The is no image for handling");
            return;
        }

        try {
            // Extract images từ row
            byte[] imageBytes = imageData.getImageBytes();
            String imageNames = imageData.getImageName();
            String imageType = imageData.getImageMimeType();


            // Nếu có images, upload và tạo Attachment records
            if (!(imageBytes.length == 0)) {
                attachment = uploadImages(imageBytes, imageNames, entityType, entityId, createdBy, imageType);
                log.info("Handled {} images for {} id={}", Arrays.toString(imageBytes), entityType, entityId);
            }

        } catch (Exception e) {
            log.error("Error handling images", e.getMessage(), e);
            // Don't throw - ảnh import fail không nên block dữ liệu chính
        }

    }

    /**
     * Upload batch images và tạo Attachment records
     * 
     * Flow:
     * 1. Validate input
     * 2. Dùng AttachmentService.uploadAttachments() để upload tất cả
     * 3. Return list attachment records
     */
    @Override
    @Transactional
    public Attachment uploadImages(
            byte[] imageByte,
            String imageName,
            String entityType,
            Long entityId,
            String createdBy,
            String imageType) {

        Attachment attachments = new Attachment();

        if (imageByte.length == 0) {
            log.debug("No images to upload");
            return attachments;
        }
        // Validate entityType
        try {
            // Upload ảnh

                try {
                    log.info("Uploading image {} for {} id={}", imageName, entityType, entityId);

                    // Create in-memory MultipartFile from bytes
                    InMemoryMultipartFile multipartFile = new InMemoryMultipartFile(
                        imageName,
                        imageByte,
                        imageType
                    );

                    // Upload via AttachmentService
                    Attachment attachment = attachmentService.uploadAttachment(
                        multipartFile,
                        entityType,
                        entityId,
                        createdBy
                    );

                    log.info("Image uploaded successfully: attachment id={}", attachment.getId());

                } catch (Exception e) {
                    log.error("Failed to upload image {}: {}", imageName, e.getMessage());
                    // Continue với images khác
                }


        } catch (Exception e) {
            log.error("Error uploading batch images: {}", e.getMessage(), e);
        }

        return attachments;
    }
}

/**
 * Helper class để convert byte[] → MultipartFile
 * Dùng cho việc upload images extracted từ Excel
 */
class InMemoryMultipartFile implements MultipartFile {

    private final String name;
    private final byte[] content;
    private final String contentType;

    public InMemoryMultipartFile(String name, byte[] content, String contentType) {
        this.name = name;
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content != null ? content.length : 0;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public java.io.InputStream getInputStream() {
        return new java.io.ByteArrayInputStream(content != null ? content : new byte[0]);
    }

    @Override
    public void transferTo(java.io.File dest) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void transferTo(java.nio.file.Path dest) {
        throw new UnsupportedOperationException("Not supported");
    }
}


