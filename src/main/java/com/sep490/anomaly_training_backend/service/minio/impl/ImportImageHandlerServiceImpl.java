package com.sep490.anomaly_training_backend.service.minio.impl;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
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

    private final AttachmentRepository attachmentRepository;
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

    /**
     * Xoá tất cả attachments của một entity
     * 
     * Flow:
     * 1. Find all attachments by entityType + entityId
     * 2. Set deleteFlag = true cho mỗi cái
     * 3. Save lại
     */


//    /**
//     * Xoá một attachment cụ thể
//     */
//    @Override
//    @Transactional
//    public void deleteAttachment(Long attachmentId) {
//        try {
//            log.info("Deleting attachment id={}", attachmentId);
//
//            attachmentRepository.deleteById(attachmentId);
//
//            log.info("Attachment deleted");
//
//        } catch (Exception e) {
//            log.error("Error deleting attachment id={}: {}", attachmentId, e.getMessage());
//        }
//    }
//
//    /**
//     * Lấy danh sách attachments của một entity
//     */
//    @Override
//    public List<Attachment> getAttachments(String entityType, Long entityId) {
//        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);
//    }
//
//    // ========================================================================
//    // Helper Methods
//    // ========================================================================
//
//    /**
//     * Extract images từ XSSF row (Excel 2007+)
//     *
//     * @param sheet XSSFSheet
//     * @param rowNum Row number
//     * @param imageBytes List để chứa image bytes
//     * @param imageNames List để chứa image names
//     */
//    private void extractImagesFromXSSFRow(
//            XSSFSheet sheet,
//            int rowNum,
//            List<byte[]> imageBytes,
//            List<String> imageNames) {
//
//        try {
//            Drawing<?> drawing = sheet.getDrawingPatriarch();
//
//            if (drawing == null) {
//                log.debug("No drawing found in sheet");
//                return;
//            }
//
//            if (!(drawing instanceof XSSFDrawing)) {
//                log.debug("Drawing is not XSSF drawing");
//                return;
//            }
//
//            XSSFDrawing xssfDrawing = (XSSFDrawing) drawing;
//
//            // Iterate through all shapes (images)
//            for (Object shape : xssfDrawing.getShapes()) {
//                if (shape instanceof XSSFPicture) {
//                    XSSFPicture picture = (XSSFPicture) shape;
//
//                    // Check if picture is in the same row
//                    int picRow = picture.getClientAnchor().getRow1();
//                    if (picRow == rowNum) {
//                        try {
//                            byte[] pictureData = picture.getPictureData().getData();
//                            String pictureName = "image_" + System.currentTimeMillis() + ".png";
//
//                            imageBytes.add(pictureData);
//                            imageNames.add(pictureName);
//
//                            log.debug("Extracted image from row {}: {} bytes", rowNum, pictureData.length);
//
//                        } catch (Exception e) {
//                            log.error("Error extracting picture from row {}: {}", rowNum, e.getMessage());
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("Error extracting images from XSSF row {}: {}", rowNum, e.getMessage());
//        }
//    }
//
//    /**
//     * Detect MIME type từ filename
//     */
//    private String detectMimeType(String fileName) {
//        if (fileName == null) return "application/octet-stream";
//
//        String lowerName = fileName.toLowerCase();
//
//        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
//            return "image/jpeg";
//        } else if (lowerName.endsWith(".png")) {
//            return "image/png";
//        } else if (lowerName.endsWith(".gif")) {
//            return "image/gif";
//        } else if (lowerName.endsWith(".webp")) {
//            return "image/webp";
//        }
//
//        return "application/octet-stream";
//    }
//
//    /**
//     * Validate entity type
//     */
//    private boolean isValidEntityType(String entityType) {
//        return entityType != null && (
//            entityType.equals("DEFECT") ||
//            entityType.equals("TRAINING_SAMPLE") ||
//            entityType.equals("DEFECT_PROPOSAL_DETAIL") ||
//            entityType.equals("TRAINING_SAMPLE_PROPOSAL_DETAIL")
//        );
//    }
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


