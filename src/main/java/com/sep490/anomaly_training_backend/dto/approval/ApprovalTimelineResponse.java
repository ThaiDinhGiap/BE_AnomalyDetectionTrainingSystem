package com.sep490.anomaly_training_backend.dto.approval;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO cho tiến trình phê duyệt hiển thị trên FE.
 * Dùng chung cho: TrainingPlan, DefectProposal, TrainingSampleProposal, TrainingResult
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalTimelineResponse {

    private String entityType;
    private Long entityId;
    private String currentStatus;        // Trạng thái hiện tại của entity
    private List<TimelineStep> steps;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimelineStep {

        /**
         * Thứ tự bước: -1=REVISE, 0=SUBMIT, 1=SV, 2=MG
         */
        private Integer stepOrder;

        /**
         * Nhãn hiển thị trên FE: "NGƯỜI TẠO", "NGƯỜI KIỂM TRA", "NGƯỜI PHÊ DUYỆT"
         */
        private String stepLabel;

        /**
         * DONE       – đã thực hiện (chấm xanh)
         * WAITING    – đang chờ (chấm cam)
         * REJECTED   – bị từ chối (chấm đỏ)
         * PENDING    – bước chưa đến lượt (chấm xám)
         */
        private StepState state;

        /**
         * Tên người thực hiện, null nếu chưa có action
         */
        private String performerName;

        /**
         * Mã nhân viên / username người thực hiện
         */
        private String performerCode;

        /**
         * Thời gian thực hiện
         */
        private Instant performedAt;

        /**
         * Comment hoặc tag badge hiển thị dưới tên, VD "Đã kiểm tra hợp lệ"
         */
        private String comment;

        /**
         * Action thực tế: SUBMIT, APPROVE, REJECT, REVISE
         */
        private String action;

        public enum StepState {
            DONE, WAITING, REJECTED, PENDING
        }
    }
}