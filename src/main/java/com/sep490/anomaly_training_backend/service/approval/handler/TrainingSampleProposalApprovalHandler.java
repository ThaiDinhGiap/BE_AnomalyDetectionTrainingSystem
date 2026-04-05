package com.sep490.anomaly_training_backend.service.approval.handler;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.repository.AttachmentRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.util.TrainingCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingSampleProposalApprovalHandler implements ApprovalHandler {
    private final TrainingSampleRepository trainingSampleRepository;
    private final TrainingCodeGenerator trainingCodeGenerator;
    private final AttachmentService attachmentService;
    private final TrainingSampleProposalDetailRepository proposalDetailRepository;
    private final AttachmentRepository attachmentRepository;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;

    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL;
    }

    @Override
    public String getDisplayLabel() {
        return "đề xuất mẫu huấn luyện";
    }

    @Override
    public List<OverdueItem> findOverdueItems(ReportStatus status, LocalDateTime threshold) {
        return trainingSampleProposalRepository.findByStatusAndDeleteFlagFalse(status)
                .stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(threshold))
                .map(r -> new OverdueItem(r.getId(), r.getGroupId()))
                .toList();
    }

    @Override
    public void applyApproval(Approvable entity) {
        TrainingSampleProposal trainingSampleProposal = (TrainingSampleProposal) entity;
        List<TrainingSampleProposalDetail> details = trainingSampleProposal.getDetails()
                .stream()
                .filter(detail -> !detail.isDeleteFlag())
                .toList();
        if (details == null || details.isEmpty()) {
            throw new IllegalStateException("TrainingSampleProposal has no details to apply.");
        }
        for (TrainingSampleProposalDetail d : details) {
            if (d.getProposalType() == null) {
                throw new IllegalStateException("ProposalType is null for detailId=" + d.getId());
            }

            switch (d.getProposalType()) {
                case CREATE -> applyCreate(d);
                case UPDATE -> applyUpdate(d);
                case DELETE -> applyDelete(d);
                default -> throw new IllegalStateException("Unsupported ProposalType: " + d.getProposalType());
            }
        }
    }

    /* ===================== APPLY CREATE ===================== */
    private void applyCreate(TrainingSampleProposalDetail d) {
        // CREATE: TrainingSample không được có sẵn
        if (d.getTrainingSample() != null) {
            throw new IllegalStateException("CREATE detail must not reference an existing trainingSample. detailId=" + d.getId());
        }

        // Validate fields for CREATE
        validateCreateFields(d);

        // Create a new TrainingSample object
        TrainingSample created = new TrainingSample();
        copyFromDetailToTrainingSample(d, created);

        // Generate trainingCode for new TrainingSample
        String trainingCode = trainingCodeGenerator.generateTrainingCode();
        created.setTrainingCode(trainingCode);
        log.info("Generated trainingCode: {} for new TrainingSample", trainingCode);

        // Calculate and set order fields
        Integer processOrder = calculateProcessOrder(d.getProcess().getId());
        Integer categoryOrder = calculateCategoryOrder(d.getProcess().getId(), d.getCategoryName());
        Integer contentOrder = calculateContentOrder(d.getProcess().getId(), d.getCategoryName(), d.getTrainingDescription());

        created.setProcessOrder(processOrder);
        created.setCategoryOrder(categoryOrder);
        created.setContentOrder(contentOrder);

        log.info("Set orders - processOrder: {}, categoryOrder: {}, contentOrder: {}", processOrder, categoryOrder, contentOrder);
        // Save the new TrainingSample
        created = trainingSampleRepository.save(created);
        log.info("TrainingSample created successfully with ID: {} and trainingCode: {}", created.getId(), created.getTrainingCode());
        //Save image
        List<Attachment> proposalImages = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", d.getId());
        if (proposalImages != null) {
            for (Attachment proposal : proposalImages) {
                Attachment attachment = new Attachment();
                attachment.setEntityId(created.getId());
                attachment.setEntityType("TRAINING_SAMPLE");
                attachment.setBucket(proposal.getBucket());
                attachment.setObjectKey(proposal.getObjectKey());
                attachment.setOriginalFilename(proposal.getOriginalFilename());
                attachment.setContentType(proposal.getContentType());
                attachment.setSizeBytes(proposal.getSizeBytes());
                attachment.setStatus(proposal.getStatus());
                attachment.setCreatedBy(proposal.getCreatedBy());
                attachment.setPrimary(proposal.isPrimary());
                attachment.setNote(proposal.getNote());
                attachment.setSortOrder(proposal.getSortOrder());
                attachmentRepository.save(attachment);
            }
        }
        // Set the newly created TrainingSample back to the proposal detail for auditing purposes
        d.setTrainingSample(created);
        proposalDetailRepository.save(d);
    }

    /* ===================== APPLY UPDATE ===================== */
    private void applyUpdate(TrainingSampleProposalDetail d) {
        // UPDATE: TrainingSample must exist
        if (d.getTrainingSample() == null || d.getTrainingSample().getId() == null) {
            throw new AppException(ErrorCode.MISSING_TRAINING_SAMPLE, "Không thế xoá hoặc chỉnh sửa một training sample không tồn tại");
        }

        // Find the existing TrainingSample to update
        TrainingSample existing = trainingSampleRepository.findById(d.getTrainingSample().getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINING_SAMPLE_NOT_FOUND));

        // Validate fields for UPDATE
        validateUpdateFields(d);

        // Store old values to detect changes
        Long oldProcessId = existing.getProcess().getId();
        String oldCategoryName = existing.getCategoryName();
        String oldTrainingDescription = existing.getTrainingDescription();

        // Copy new values from the proposal detail to the existing TrainingSample
        copyFromDetailToTrainingSample(d, existing);

        // Recalculate order fields if process, categoryName, or trainingDescription changed
        if (!oldProcessId.equals(d.getProcess().getId()) ||
                !oldCategoryName.equals(d.getCategoryName()) ||
                !oldTrainingDescription.equals(d.getTrainingDescription())) {

            Integer processOrder = calculateProcessOrder(d.getProcess().getId());
            Integer categoryOrder = calculateCategoryOrder(d.getProcess().getId(), d.getCategoryName());
            Integer contentOrder = calculateContentOrder(d.getProcess().getId(), d.getCategoryName(), d.getTrainingDescription());

            existing.setProcessOrder(processOrder);
            existing.setCategoryOrder(categoryOrder);
            existing.setContentOrder(contentOrder);

            log.info("Recalculated orders for TrainingSample ID {} - processOrder: {}, categoryOrder: {}, contentOrder: {}",
                    existing.getId(), processOrder, categoryOrder, contentOrder);
        }

        // Note: trainingCode should NOT be changed on UPDATE (it's a permanent identifier)
        log.info("Updating TrainingSample ID: {} with existing trainingCode: {}", existing.getId(), existing.getTrainingCode());

        // Check for unique constraint violation for updated trainingSampleCode
        if (!existing.getTrainingSampleCode().isEmpty() && trainingSampleRepository.existsByProductLineIdAndTrainingSampleCodeAndIdNot(existing.getProductLine().getId(), existing.getTrainingSampleCode(), existing.getId())) {
            throw new AppException(ErrorCode.TRAINING_SAMPLE_CODE_ALREADY_EXISTS, "trainingSampleCode already exists for this productLine. proposalDetail detailId=" + d.getId());
        }

        //Replace images if there are new ones in the proposal
        List<Attachment> proposalImages = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", d.getId());
        if (proposalImages != null || !proposalImages.isEmpty()) {
            attachmentService.deleteAttachments("TRAINING_SAMPLE", existing.getId());
            for (Attachment proposal : proposalImages) {
                Attachment attachment = new Attachment();
                attachment.setEntityId(existing.getId());
                attachment.setEntityType("TRAINING_SAMPLE");
                attachment.setBucket(proposal.getBucket());
                attachment.setObjectKey(proposal.getObjectKey());
                attachment.setOriginalFilename(proposal.getOriginalFilename());
                attachment.setContentType(proposal.getContentType());
                attachment.setSizeBytes(proposal.getSizeBytes());
                attachment.setStatus(proposal.getStatus());
                attachment.setCreatedBy(proposal.getCreatedBy());
                attachment.setPrimary(proposal.isPrimary());
                attachment.setNote(proposal.getNote());
                attachment.setSortOrder(proposal.getSortOrder());
                attachmentRepository.save(attachment);
            }
        }

        // Save the updated TrainingSample
        trainingSampleRepository.save(existing);
        log.info("TrainingSample updated successfully with ID: {}", existing.getId());
    }

    /* ===================== APPLY DELETE ===================== */
    private void applyDelete(TrainingSampleProposalDetail d) {
        // DELETE: TrainingSample must exist
        if (d.getTrainingSample() == null || d.getTrainingSample().getId() == null) {
            throw new IllegalStateException("DELETE detail must reference existing trainingSample. detailId=" + d.getId());
        }

        // Find the TrainingSample to delete
        TrainingSample existing = trainingSampleRepository.findById(d.getTrainingSample().getId())
                .orElseThrow(() -> new IllegalStateException("TrainingSample not found id=" + d.getTrainingSample().getId()));

        // Soft delete (Recommended)
        existing.setDeleteFlag(true);
        trainingSampleRepository.save(existing);
        log.info("TrainingSample soft deleted successfully with ID: {} and trainingCode: {}", existing.getId(), existing.getTrainingCode());

        // If hard delete is required (be cautious with this!)
        // trainingSampleRepository.delete(existing);
    }

    /* ===================== FIELD VALIDATION ===================== */

    // For CREATE validation
    private void validateCreateFields(TrainingSampleProposalDetail d) {
        if (d.getProcess() == null) {
            throw new IllegalStateException("Process is required for CREATE. detailId=" + d.getId());
        }
        if (d.getCategoryName() == null || d.getCategoryName().isEmpty()) {
            throw new IllegalStateException("CategoryName is required for CREATE. detailId=" + d.getId());
        }
        if (d.getTrainingDescription() == null || d.getTrainingDescription().isEmpty()) {
            throw new IllegalStateException("TrainingDescription is required for CREATE. detailId=" + d.getId());
        }
    }

    // For UPDATE validation
    private void validateUpdateFields(TrainingSampleProposalDetail d) {
        validateCreateFields(d);  // In this case, same validation as CREATE
    }

    /* ===================== MAPPING HELPER ===================== */

    // Copy fields from proposal detail to the TrainingSample entity
    private void copyFromDetailToTrainingSample(TrainingSampleProposalDetail d, TrainingSample target) {
        target.setProcess(d.getProcess());
        target.setProductLine(d.getProcess().getProductLine());
        target.getProducts().addAll(d.getProducts());
        target.setDefect(d.getDefect());
        target.setCategoryName(d.getCategoryName());
        target.setTrainingDescription(d.getTrainingDescription());
        target.setNote(d.getNote());
        target.setTrainingSampleCode(d.getTrainingSampleCode());
    }

    /* ===================== ORDER CALCULATION HELPERS ===================== */


    private Integer calculateProcessOrder(Long processId) {
        // Check if this process already has an order assigned
        java.util.Optional<Integer> existingOrder = trainingSampleRepository.findProcessOrderByProcessId(processId);
        if (existingOrder.isPresent()) {
            log.debug("ProcessId {} already has processOrder: {}", processId, existingOrder.get());
            return existingOrder.get();
        }

        // If not, get max processOrder across all processes and increment
        Integer maxProcessOrder = trainingSampleRepository.findMaxProcessOrderByProcessId(processId);
        Integer newProcessOrder = maxProcessOrder + 1;
        log.debug("Assigning new processOrder {} for processId {}", newProcessOrder, processId);
        return newProcessOrder;
    }


    private Integer calculateCategoryOrder(Long processId, String categoryName) {
        // Check if this (process, category) combination already exists
        java.util.Optional<Integer> existingOrder = trainingSampleRepository.findCategoryOrderByProcessAndCategory(processId, categoryName);
        if (existingOrder.isPresent()) {
            log.debug("ProcessId {} + CategoryName {} already has categoryOrder: {}", processId, categoryName, existingOrder.get());
            return existingOrder.get();
        }

        // If not, get max categoryOrder for this process and increment
        Integer maxCategoryOrder = trainingSampleRepository.findMaxCategoryOrderByProcessAndCategory(processId, categoryName);
        Integer newCategoryOrder = maxCategoryOrder + 1;
        log.debug("Assigning new categoryOrder {} for processId {} + categoryName {}", newCategoryOrder, processId, categoryName);
        return newCategoryOrder;
    }


    private Integer calculateContentOrder(Long processId, String categoryName, String trainingDescription) {
        // Check if this (process, category, description) combination already exists
        java.util.Optional<Integer> existingOrder = trainingSampleRepository.findContentOrderByProcessCategoryAndDescription(processId, categoryName, trainingDescription);
        if (existingOrder.isPresent()) {
            log.debug("ProcessId {} + CategoryName {} + TrainingDescription {} already has contentOrder: {}",
                    processId, categoryName, trainingDescription, existingOrder.get());
            return existingOrder.get();
        }

        // If not, get max contentOrder for this (process, category) and increment
        Integer maxContentOrder = trainingSampleRepository.findMaxContentOrderByProcessCategoryAndDescription(processId, categoryName, trainingDescription);
        Integer newContentOrder = maxContentOrder + 1;
        log.debug("Assigning new contentOrder {} for processId {} + categoryName {} + trainingDescription {}",
                newContentOrder, processId, categoryName, trainingDescription);
        return newContentOrder;
    }
}
