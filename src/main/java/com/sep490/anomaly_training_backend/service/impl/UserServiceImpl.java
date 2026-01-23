package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.dto.request.UserRequest;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.mapper.UserMapper;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        // Dùng toEntity
        User user = userMapper.toEntity(request);

        // Save và dùng toDTO
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        userMapper.updateEntity(user, request);

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setDeleteFlag(true);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleteFlag())
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isDeleteFlag())
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
}