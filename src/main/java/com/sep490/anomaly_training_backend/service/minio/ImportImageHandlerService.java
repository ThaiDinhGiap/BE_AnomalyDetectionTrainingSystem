package com.sep490.anomaly_training_backend.service.minio;

import com.sep490.anomaly_training_backend.model.Attachment;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

/**
 * Service để xử lý image import từ Excel
 * - Extract images từ rows
 * - Upload lên MinIO
 * - Tạo Attachment records
 * 
 * Được sử dụng chung bởi cả Defect và TrainingSample import
 */
public interface ImportImageHandlerService {

    /**
     * Xử lý images từ một dòng Excel
     * - Extract images từ row
     * - Validate image
     * - Upload lên storage
     * - Tạo Attachment records trong DB
     * 
     * @param row Row từ Excel sheet
     * @param entityType Loại entity (e.g., "DEFECT", "TRAINING_SAMPLE")
     * @param entityId ID của entity (defect.id hoặc trainingSample.id)
     * @param createdBy Username của user upload
     * @return List<Attachment> records được tạo (có thể empty nếu row không có image)
     */
    List<Attachment> handleRowImages(
        Row row,
        String entityType,
        Long entityId,
        String createdBy
    );

    /**
     * Upload batch images và tạo Attachment records
     * 
     * @param imageBytes List image bytes
     * @param imageNames List original file names
     * @param entityType Loại entity
     * @param entityId ID của entity
     * @param createdBy Username
     * @return List<Attachment> records được tạo
     */
    List<Attachment> uploadImages(
        List<byte[]> imageBytes,
        List<String> imageNames,
        String entityType,
        Long entityId,
        String createdBy
    );

    /**
     * Xoá tất cả attachments của một entity (chuẩn bị cho update)
     * - Soft delete (set deleteFlag = true)
     * - Không xoá physical file (để background job xử lý)
     * 
     * @param entityType Loại entity
     * @param entityId ID của entity
     */
    void deleteAttachments(String entityType, Long entityId);

    /**
     * Xoá attachment cụ thể
     * 
     * @param attachmentId ID của attachment
     */
    void deleteAttachment(Long attachmentId);

    /**
     * Lấy danh sách attachments của một entity
     * 
     * @param entityType Loại entity
     * @param entityId ID của entity
     * @return List<Attachment>
     */
    List<Attachment> getAttachments(String entityType, Long entityId);
}


