package com.sep490.anomaly_training_backend.service.minio;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    /**
     * Uploads a file to the storage.
     *
     * @param file the file to upload
     * @param objectKey the key of the object in the storage
     * @return the URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String objectKey);

    /**
     * Deletes a file from the storage.
     *
     * @param objectKey the key of the object to delete
     */
    void deleteFile(String objectKey);

    String getFileUrl(String objectKey);
}
