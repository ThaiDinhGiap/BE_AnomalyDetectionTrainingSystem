package com.sep490.anomaly_training_backend.service.account;

import com.sep490.anomaly_training_backend.dto.request.UserRequest;
import com.sep490.anomaly_training_backend.dto.response.UserDashboard;
import com.sep490.anomaly_training_backend.dto.response.UserResponse;
import com.sep490.anomaly_training_backend.model.Employee;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    List<UserDashboard> getAllUserDashboard();

    Employee getEmployeeOfUser(Long userId);
}