package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.model.Attachment;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Attachment> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            Authentication authentication) {

        // Assuming the user's name/ID is stored in the authentication principal
        String createdBy = authentication.getName();

        Attachment attachment = attachmentService.uploadAttachment(file, entityType, entityId, createdBy);
        return ResponseEntity.ok(attachment);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Attachment>> getAttachments(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {
        List<Attachment> attachments = attachmentService.getAttachmentsByEntity(entityType, entityId);
        return ResponseEntity.ok(attachments);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAttachment(@PathVariable("id") Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/uploads")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Attachment>> uploadAttachments(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            Authentication authentication) {

        String createdBy = authentication.getName();

        List<Attachment> attachments = attachmentService.uploadAttachments(files, entityType, entityId, createdBy);

        return ResponseEntity.ok(attachments);
    }
}
