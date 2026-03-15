package com.sep490.anomaly_training_backend.service.minio;

import com.sep490.anomaly_training_backend.dto.request.ImageData;
import com.sep490.anomaly_training_backend.model.Attachment;

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
     * @param entityType Loại entity (e.g., "DEFECT", "TRAINING_SAMPLE")
     * @param entityId   ID của entity (defect.id hoặc trainingSample.id)
     * @param createdBy  Username của user upload
     */
    void handleRowImages(
        ImageData imageData,
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
    Attachment uploadImages(
        byte[] imageBytes,
        String imageNames,
        String entityType,
        Long entityId,
        String createdBy,
        String imageType
    );
}


