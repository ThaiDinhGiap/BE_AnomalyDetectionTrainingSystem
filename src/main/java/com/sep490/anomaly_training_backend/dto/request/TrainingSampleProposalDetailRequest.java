package com.sep490.anomaly_training_backend.dto.request;

import com.sep490.anomaly_training_backend.enums.ProposalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingSampleProposalDetailRequest {
    Long id;
    Long trainingSampleId; // chỉ định mẫu huan luyen nào đc tác động
    ProposalType proposalType; //CREATE, UPDATE, DELETE
    Long processId; //Công doan
    Long defectId; // Loi Quá khứ
    String categoryName; // Hạng Mục
    String trainingSampleCode; //Mã mấu
    Long productId; // Mã sản phẩm
    String trainingDescription; // Nôi dung
    String note; //Chi chú
}
