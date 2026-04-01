package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApprovalHandlerRegistryTest {

    @Test
    void getHandler_ShouldReturnRegisteredHandler() {
        ApprovalHandler mockHandler = mock(ApprovalHandler.class);
        when(mockHandler.getType()).thenReturn(ApprovalEntityType.DEFECT_PROPOSAL);

        ApprovalHandlerRegistry registry = new ApprovalHandlerRegistry(List.of(mockHandler));

        ApprovalHandler handler = registry.getHandler(ApprovalEntityType.DEFECT_PROPOSAL);
        assertThat(handler).isNotNull();
        assertThat(handler.getType()).isEqualTo(ApprovalEntityType.DEFECT_PROPOSAL);
    }

    @Test
    void getHandler_WhenNotRegistered_ShouldThrowException() {
        ApprovalHandlerRegistry registry = new ApprovalHandlerRegistry(List.of());

        assertThatThrownBy(() -> registry.getHandler(ApprovalEntityType.TRAINING_PLAN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No ApprovalHandler registered for type: TRAINING_PLAN");
    }
}
