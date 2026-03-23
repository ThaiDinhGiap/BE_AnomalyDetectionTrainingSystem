package com.sep490.anomaly_training_backend.service.approval.helper;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.DefectType;
import com.sep490.anomaly_training_backend.enums.ProcessClassification;
import com.sep490.anomaly_training_backend.model.*;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.*;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.service.minio.AttachmentService;
import com.sep490.anomaly_training_backend.util.DefectCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectProposalApprovalHandler implements ApprovalHandler {
    private final DefectProposalRepository defectProposalRepository;
    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final DefectRepository defectRepository;
    private final DefectCodeGenerator defectCodeGenerator;
    private final ProcessRepository processRepository;
    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    @Override
    public ApprovalEntityType getType() {
        return ApprovalEntityType.DEFECT_PROPOSAL;
    }

    @Override
    public void applyApproval(Approvable entity) {
        DefectProposal proposal = (DefectProposal) entity;
        List<DefectProposalDetail> detailList = proposal.getDetails();
        if (detailList == null || detailList.isEmpty()) {
            throw new IllegalStateException("Proposal has no details to apply.");
        }
        // ---) Apply từng detail
        for (DefectProposalDetail d : detailList) {

            if (d.getProposalType() == null) {
                throw new IllegalStateException("ProposalType is missing in proposal detail id=" + d.getId());
            }

            Process process = d.getProcess();
            if (d.getDefectType().equals(DefectType.DEFECTIVE_GOODS) && (process.getClassification().getValue() != 1)) {
                d.getProcess().setClassification(ProcessClassification.C1);
            }
            processRepository.save(process);

            switch (d.getProposalType()) {
                case CREATE -> {
                    // Rule: CREATE => defect must be null
                    if (d.getDefect() != null) {
                        throw new IllegalStateException("CREATE detail must not reference an existing defect. detailId=" + d.getId());
                    }

                    Defect created = new Defect();
                    copyFromDetailToDefect(d, created);

                    // Tạo defect code tự động
                    String generatedCode = defectCodeGenerator.generateDefectCode();
                    created.setDefectCode(generatedCode);

                    // đảm bảo không null các field bắt buộc
                    requireNonNullForCreate(created, d);

                    created = defectRepository.save(created);
                    uploadAttachmentToDefect(created, d);
                    // Nếu muốn lưu lại defect vừa tạo vào detail để audit:
                    d.setDefect(created);
                }

                case UPDATE -> {
                    // Rule: UPDATE => defect must exist
                    if (d.getDefect() == null || d.getDefect().getId() == null) {
                        throw new IllegalStateException("UPDATE detail must reference an existing defect. detailId=" + d.getId());
                    }

                    Defect defect = defectRepository.findById(d.getDefect().getId())
                            .orElseThrow(() -> new IllegalStateException("Defect not found id=" + d.getDefect().getId()));

                    copyFromDetailToDefect(d, defect);
                    // Giữ nguyên defect code khi UPDATE, không tạo code mới
                    requireNonNullForUpdate(defect, d);

                    //Replace images if there are new ones in the proposal
                    List<Attachment> proposalImages = attachmentService.getAttachmentsByEntity("TRAINING_SAMPLE_PROPOSAL", d.getId());
                    if (proposalImages != null) {
                        attachmentService.deleteAttachments("TRAINING_SAMPLE", defect.getId());
                        for (Attachment proposalImage : proposalImages) {
                            Attachment attachment = new Attachment();
                            attachment.setEntityId(defect.getId());
                            attachment.setEntityType("TRAINING_SAMPLE");
                            attachment.setBucket(proposalImage.getBucket());
                            attachment.setObjectKey(proposalImage.getObjectKey());
                            attachment.setOriginalFilename(proposalImage.getOriginalFilename());
                            attachment.setContentType(proposalImage.getContentType());
                            attachment.setSizeBytes(proposalImage.getSizeBytes());
                            attachment.setStatus(proposalImage.getStatus());
                            attachment.setCreatedBy(proposalImage.getCreatedBy());
                            attachment.setPrimary(proposalImage.isPrimary());
                            attachment.setNote(proposalImage.getNote());
                            attachment.setSortOrder(proposalImage.getSortOrder());
                            attachmentRepository.save(attachment);
                        }
                    }

                    Defect updated = defectRepository.save(defect);
                    uploadAttachmentToDefect(updated, d);
                }

                case DELETE -> {
                    // Rule: DELETE => defect must exist
                    if (d.getDefect() == null || d.getDefect().getId() == null) {
                        throw new IllegalStateException("DELETE detail must reference an existing defect. detailId=" + d.getId());
                    }

                    Defect defect = defectRepository.findById(d.getDefect().getId())
                            .orElseThrow(() -> new IllegalStateException("Defect not found id=" + d.getDefect().getId()));

                    // --- Soft delete khuyến nghị
                    // Tuỳ BaseEntity bạn có field gì:
                    // defect.setIsActive(false);
                    // defect.setDeletedAt(LocalDateTime.now());

                    // Nếu bạn chưa có soft delete, dùng hard delete:
                    defectRepository.delete(defect);
                }

                default -> throw new IllegalStateException("Unsupported ProposalType: " + d.getProposalType());
            }
        }
        proposal.setUpdatedAt(LocalDateTime.now());
        defectProposalRepository.save(proposal);

    }

    private void copyFromDetailToDefect(DefectProposalDetail d, Defect defect) {
        defect.setDefectDescription(d.getDefectDescription());
        defect.setProcess(d.getProcess());              // đảm bảo process là entity managed
        defect.setDetectedDate(d.getDetectedDate());
        defect.setNote(d.getNote());
        defect.setOriginCause(d.getOriginCause());
        defect.setOutflowCause(d.getOutflowCause());
        defect.setCausePoint(d.getCausePoint());
        defect.setOriginMeasures(d.getOriginMeasures());
        defect.setOutflowMeasures(d.getOutflowMeasures());
        defect.setDefectType(d.getDefectType());
        defect.setProduct(d.getProduct());
    }

    private void requireNonNullForCreate(Defect defect, DefectProposalDetail d) {
        if (StringUtil.isBlank(defect.getDefectDescription())) {
            throw new IllegalStateException("defectDescription is required for CREATE. detailId=" + d.getId());
        }
        if (defect.getProcess() == null) {
            throw new IllegalStateException("process is required for CREATE. detailId=" + d.getId());
        }
        if (defect.getDetectedDate() == null) {
            throw new IllegalStateException("detectedDate is required for CREATE. detailId=" + d.getId());
        }
        if (StringUtil.isBlank(defect.getDefectCode())) {
            throw new IllegalStateException("defectCode should be generated and must not be blank. detailId=" + d.getId());
        }
    }

    private void requireNonNullForUpdate(Defect defect, DefectProposalDetail d) {
        // Nếu bạn chọn UPDATE theo full snapshot, thì check giống CREATE
        requireNonNullForCreate(defect, d);
    }

    private void uploadAttachmentToDefect(Defect defect, DefectProposalDetail proposalDefect) {
        //Create and update image to defect
        List<Attachment> originAttachments = attachmentService.getAttachmentsByEntity("DEFECT", defect.getId());
        List<Attachment>  proposalAttachments = attachmentService.getAttachmentsByEntity("DEFECT_PROPOSAL", proposalDefect.getId());
        if (originAttachments != null) {
            for (Attachment origin : originAttachments) {
                attachmentService.deleteAttachment(origin.getId());
            }
        }
        for (Attachment proposal : proposalAttachments) {
            Attachment attachment = new Attachment();
            attachment.setEntityId(defect.getId());
            attachment.setEntityType("DEFECT");
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
}
