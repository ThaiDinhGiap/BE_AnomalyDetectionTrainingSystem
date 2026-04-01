package com.sep490.anomaly_training_backend.service.account.impl;

import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.UserMapper;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.ProductLine;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.EmployeeRepository;
import com.sep490.anomaly_training_backend.repository.ProductLineRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProductLineRepository productLineRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setIsActive(true);
        mockUser.setDeleteFlag(false);
    }

    @Test
    void deleteUser_ShouldSetFlagsAndSave() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        userService.deleteUser(1L);

        assertThat(mockUser.isDeleteFlag()).isTrue();
        assertThat(mockUser.getIsActive()).isFalse();
        verify(userRepository).save(mockUser);
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void getAllUserDashboard_ShouldReturnMappedList() {
        when(userRepository.findAllUsersWithRoles()).thenReturn(List.of(mockUser));
        
        UserDashboard dashboard = UserDashboard.builder()
                .id(1L)
                .username("testuser")
                .build();
        when(userMapper.toUserDashboard(mockUser)).thenReturn(dashboard);

        List<UserDashboard> result = userService.getAllUserDashboard();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void getTeamLeadInProductLine_ShouldReturnTeamLeads() {
        Group group = new Group();
        group.setId(10L);

        ProductLine productLine = new ProductLine();
        productLine.setId(100L);
        productLine.setGroup(group);

        User leadUser = new User();
        leadUser.setId(2L);
        leadUser.setUsername("teamlead");

        Team team = new Team();
        team.setId(1000L);
        team.setTeamLeader(leadUser);

        when(productLineRepository.findById(100L)).thenReturn(Optional.of(productLine));
        when(teamRepository.findByGroupId(10L)).thenReturn(List.of(team));
        
        UserResponse response = new UserResponse();
        response.setId(2L);
        response.setUsername("teamlead");
        when(userMapper.toDTO(leadUser)).thenReturn(response);

        List<UserResponse> result = userService.getTeamLeadInProductLine(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("teamlead");
    }

    @Test
    void getTeamLeadInProductLine_ProductLineNotFound_ShouldThrowException() {
        when(productLineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getTeamLeadInProductLine(999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_LINE_NOT_FOUND.getMessage());
    }
}
