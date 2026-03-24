package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByUsernameAndDeleteFlagFalse(String username);

    Optional<User> findByEmailAndDeleteFlagFalse(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username AND u.deleteFlag = false")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email AND u.deleteFlag = false")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :id AND u.deleteFlag = false")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    Optional<User> findByOauthProviderAndOauthProviderIdAndDeleteFlagFalse(OAuthProvider oauthProvider, String oauthProviderId);

    boolean existsByUsernameAndDeleteFlagFalse(String username);

    boolean existsByEmailAndDeleteFlagFalse(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.deleteFlag = false")
    List<User> findAllUsersWithRoles();

    // Kiểm tra trùng email
    boolean existsByEmail(String email);

    // Kiểm tra nhân viên đã có tài khoản chưa dựa vào employeeCode
    boolean existsByEmployeeCode(String employeeCode);

    Optional<User> findByEmployeeCodeAndDeleteFlagFalse(String employeeCode);
}

