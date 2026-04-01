package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void deleteGroup_ShouldSoftDelete() {
        Group group = new Group();
        group.setId(10L);
        group.setDeleteFlag(false);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        groupService.deleteGroup(10L);

        assertThat(group.isDeleteFlag()).isTrue();
        verify(groupRepository).save(group);
    }

    @Test
    void deleteGroup_WhenNotFound_ShouldThrow() {
        when(groupRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.deleteGroup(10L))
                .isInstanceOf(AppException.class);
    }

}
