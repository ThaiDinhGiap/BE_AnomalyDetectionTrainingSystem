package com.sep490.anomaly_training_backend.repository;

import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.model.UserSignaturePin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSignaturePinRepository extends JpaRepository<UserSignaturePin, Long> {

    Optional<UserSignaturePin> findByUser(User user);

    Optional<UserSignaturePin> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}