package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.response.TeamResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.mapper.TeamMapper;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team team;
    private TeamRequest request;
    private User leader;
    private Group group;

    @BeforeEach
    void setUp() {
        leader = new User();
        leader.setId(1L);
        
        group = new Group();
        group.setId(2L);

        team = new Team();
        team.setId(10L);
        team.setName("TEAM-A");
        team.setCode("TM001");

        request = new TeamRequest();
        request.setName("TEAM-A-NEW");
        request.setCode("TM002");
        request.setTeamLeaderId(1L);
        request.setGroupId(2L);
    }

    @Test
    void createTeam_WhenNameExists_ShouldThrow() {
        when(teamRepository.existsByName(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void createTeam_Success() {
        when(teamRepository.existsByName(request.getName())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
        
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        
        TeamResponse response = new TeamResponse();
        when(teamMapper.toDTO(team)).thenReturn(response);

        TeamResponse result = teamService.createTeam(request);

        assertThat(result).isNotNull();
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void updateTeam_WhenNotFound_ShouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.updateTeam(10L, request))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateTeam_Success() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
        
        when(teamRepository.save(team)).thenReturn(team);
        TeamResponse response = new TeamResponse();
        when(teamMapper.toDTO(team)).thenReturn(response);

        TeamResponse result = teamService.updateTeam(10L, request);

        assertThat(result).isNotNull();
        assertThat(team.getName()).isEqualTo("TEAM-A-NEW");
        assertThat(team.getTeamLeader().getId()).isEqualTo(1L);
        assertThat(team.getGroup().getId()).isEqualTo(2L);
        verify(teamRepository).save(team);
    }

    @Test
    void deleteTeam_WhenNotFound_ShouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeam(10L))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deleteTeam_Success() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        teamService.deleteTeam(10L);

        assertThat(team.isDeleteFlag()).isTrue();
        verify(teamRepository).save(team);
    }

    @Test
    void deleteTeamFromGroup_WhenNotFound_ShouldThrow() {
        when(teamRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeamFromGroup(10L))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deleteTeamFromGroup_Success() {
        team.setGroup(group);
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        teamService.deleteTeamFromGroup(10L);

        assertThat(team.getGroup()).isNull();
        verify(teamRepository).save(team);
    }
}
