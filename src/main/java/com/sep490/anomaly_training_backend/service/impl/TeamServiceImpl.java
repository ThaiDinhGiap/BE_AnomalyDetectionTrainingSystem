package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.response.TeamResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.TeamMapper;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        if (teamRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.TEAM_NAME_ALREADY_EXISTS);
        }

        Team team = teamMapper.toEntity(request);
        return teamMapper.toDTO(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        team.setName(request.getName());
        team.setCode(request.getCode());
        team.setTeamLeader(userRepository.findById(request.getTeamLeaderId()).orElse(null));

        teamRepository.save(team);

        return teamMapper.toDTO(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        team.setDeleteFlag(true);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public void deleteTeamFromGroup(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));

        team.setGroup(null);

        teamRepository.save(team);
    }

    @Override
    public TeamResponse getTeamById(Long id) {
        return teamRepository.findById(id)
                .filter(t -> !t.isDeleteFlag())
                .map(teamMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .filter(t -> !t.isDeleteFlag())
                .map(teamMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamResponse> getTeamsByGroup(Long groupId) {
        return teamRepository.findByGroupId(groupId).stream()
                .filter(t -> !t.isDeleteFlag())
                .map(teamMapper::toDTO)
                .collect(Collectors.toList());
    }
}