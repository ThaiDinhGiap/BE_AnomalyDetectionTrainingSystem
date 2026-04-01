package com.sep490.anomaly_training_backend.service;

import com.sep490.anomaly_training_backend.dto.request.TeamRequest;
import com.sep490.anomaly_training_backend.dto.response.TeamResponse;

public interface TeamService {
    TeamResponse createTeam(TeamRequest request);

    TeamResponse updateTeam(Long id, TeamRequest request);

    void deleteTeam(Long id);

    void deleteTeamFromGroup(Long teamId);

//    TeamResponse getTeamById(Long id);
//
//    List<TeamResponse> getAllTeams();
//
//    List<TeamResponse> getTeamsByGroup(Long groupId);
}