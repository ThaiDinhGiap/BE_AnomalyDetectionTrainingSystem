package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;

public interface ApprovalTimelineService {

    /**
     * Lấy tiến trình phê duyệt của một entity.
     *
     * @param entityType loại entity: TRAINING_PLAN, DEFECT_PROPOSAL, ...
     * @param entityId   ID của entity
     * @return danh sách các bước với trạng thái và thông tin người thực hiện
     */
    ApprovalTimelineResponse getTimeline(ApprovalEntityType entityType, Long entityId);
}