package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSampleProposalDetailRequest {
    Long trainingSampleProposalDetailId;
    Long trainingSampleId; // chỉ định mẫu huan luyen nào đc tác động
    ProposalType proposalType; // Optional: FE gửi khi muốn explicit (vd DELETE nhóm), null thì BE tự infer
    Long processId; //Công doan
    Long defectId; // Loi Quá khứ
    String categoryName; // Hạng Mục
    String trainingSampleCode; //Mã mấu
    List<Long> productIds; // Mã sản phẩm (ManyToMany)
    String trainingDescription; // Nôi dung
    String note; //Chi chú
    
    // Images to upload and store in Attachment table
    List<MultipartFile> images;
}
