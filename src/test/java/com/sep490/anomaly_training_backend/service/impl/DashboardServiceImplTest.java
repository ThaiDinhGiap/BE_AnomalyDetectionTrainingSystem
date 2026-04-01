package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.response.dashboard.ProcessFlowItem;
import com.sep490.anomaly_training_backend.enums.ProcessClassification;
import com.sep490.anomaly_training_backend.model.Process;
import com.sep490.anomaly_training_backend.repository.ProcessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ProcessRepository processRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getProcessFlow_ShouldReturnSortedList() {
        Process p1 = new Process();
        p1.setId(1L);
        p1.setCode("P02");
        p1.setName("Process A");
        p1.setClassification(ProcessClassification.C4);

        Process p2 = new Process();
        p2.setId(2L);
        p2.setCode("P01");
        p2.setName("Process B");
        p2.setClassification(ProcessClassification.C4);

        when(processRepository.findByProductLineIdAndDeleteFlagFalse(5L)).thenReturn(List.of(p1, p2));

        List<ProcessFlowItem> result = dashboardService.getProcessFlow(5L);

        // Should sort by code
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("P01");
        assertThat(result.get(1).getCode()).isEqualTo("P02");
    }
}
