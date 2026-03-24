package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.GroupRequest;
import com.sep490.anomaly_training_backend.dto.response.GroupResponse;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.mapper.GroupMapper;
import com.sep490.anomaly_training_backend.model.Group;
import com.sep490.anomaly_training_backend.model.Team;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.GroupRepository;
import com.sep490.anomaly_training_backend.repository.SectionRepository;
import com.sep490.anomaly_training_backend.repository.TeamRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;

    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        if (groupRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.GROUP_NAME_ALREADY_EXISTS);
        }

        Group group = new Group();

        if (request.getName() != null && !request.getName().equals(group.getName())) {
            group.setName(request.getName());
        }
        if (request.getCode() != null && !request.getCode().equals(group.getCode())) {
            group.setCode(request.getCode());
        }
        if (request.getSupervisorId() != null && (group.getSupervisor() == null || !request.getSupervisorId().equals(group.getSupervisor().getId()))) {
            group.setSupervisor(userRepository.findById(request.getSectionId()).orElse(null));
        }

        return groupMapper.toDTO(groupRepository.save(group));
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(Long id, GroupRequest request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        if (request.getName() != null && !request.getName().equals(group.getName())) {
            group.setName(request.getName());
        }
        if (request.getCode() != null && !request.getCode().equals(group.getCode())) {
            group.setCode(request.getCode());
        }
        if (request.getSupervisorId() != null && (group.getSupervisor() == null || !request.getSupervisorId().equals(group.getSupervisor().getId()))) {
            group.setSupervisor(userRepository.findById(request.getSectionId()).orElse(null));
        }
        if (request.getSectionId() != null && (group.getSection() == null || !request.getSectionId().equals(group.getSection().getId()))) {
            group.setSection(sectionRepository.findById(request.getSectionId()).orElse(null));
        }

        return groupMapper.toDTO(groupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        group.setDeleteFlag(true);
        groupRepository.save(group);
    }

    @Override
    @Transactional
    public void removeGroupFromSection(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        group.setSection(null);
        groupRepository.save(group);
    }

    @Override
    public GroupResponse getGroupById(Long id) {
        return groupRepository.findById(id)
                .filter(g -> !g.isDeleteFlag())
                .map(groupMapper::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Override
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .filter(g -> !g.isDeleteFlag())
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getGroupsBySection(Long sectionId) {
        return groupRepository.findBySectionId(sectionId).stream()
                .filter(g -> !g.isDeleteFlag())
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getGroupByTeamLead(Long teamLeadId) {
        return groupRepository.findByTeamLeadId(teamLeadId).stream().map(groupMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getGroupsBySupervisor(Long supervisorId) {
        return groupRepository.findBySupervisorId(supervisorId).stream().map(groupMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<GroupResponse> getMyManagedGroups() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Team> managedTeams = teamRepository.findAllByTeamLeaderId(currentUser.getId());

        return managedTeams.stream()
                .map(Team::getGroup)
                .distinct()
                .map(group -> new GroupResponse(group.getId(), group.getName()))
                .collect(Collectors.toList());
    }
}