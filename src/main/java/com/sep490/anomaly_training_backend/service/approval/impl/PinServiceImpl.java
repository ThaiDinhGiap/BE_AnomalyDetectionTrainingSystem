package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.model.UserSignaturePin;
import com.sep490.anomaly_training_backend.repository.UserSignaturePinRepository;
import com.sep490.anomaly_training_backend.service.approval.PinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PinServiceImpl implements PinService {

    private final UserSignaturePinRepository pinRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${approval.pin.max-attempts:5}")
    private int maxAttempts;

    @Value("${approval.pin.lock-minutes:30}")
    private int lockMinutes;

    @Value("${approval.pin.expire-days:90}")
    private int expireDays;

    @Override
    @Transactional
    public void setupPin(User user, String pin) {
        validatePinFormat(pin);

        UserSignaturePin signaturePin = pinRepo.findByUser(user)
                .orElse(UserSignaturePin.builder().user(user).build());

        signaturePin.setPinHash(passwordEncoder.encode(pin));
        signaturePin.setLastChangedAt(Instant.now());
        signaturePin.setExpiresAt(Instant.now().plus(expireDays, ChronoUnit.DAYS));
        signaturePin.resetFailedAttempts();

        pinRepo.save(signaturePin);
        log.info("PIN setup/changed for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void verifyPin(User user, String pin) {
        UserSignaturePin signaturePin = pinRepo.findByUser(user)
                .orElseThrow(() -> new BusinessException("Bạn chưa thiết lập PIN. Vui lòng thiết lập PIN trước khi ký."));

        // Check locked
        if (signaturePin.isLocked()) {
            throw new BusinessException("PIN đã bị khóa do nhập sai quá nhiều lần. Vui lòng thử lại sau.");
        }

        // Check expired
        if (signaturePin.isExpired()) {
            throw new BusinessException("PIN đã hết hạn. Vui lòng đổi PIN mới.");
        }

        // Verify
        if (!passwordEncoder.matches(pin, signaturePin.getPinHash())) {
            signaturePin.incrementFailedAttempts(maxAttempts, lockMinutes);
            pinRepo.save(signaturePin);

            int remaining = maxAttempts - signaturePin.getFailedAttempts();
            if (remaining > 0) {
                throw new BusinessException("PIN không đúng. Còn " + remaining + " lần thử.");
            } else {
                throw new BusinessException("PIN đã bị khóa do nhập sai quá nhiều lần.");
            }
        }

        // Success - reset failed attempts
        signaturePin.resetFailedAttempts();
        pinRepo.save(signaturePin);
    }

    @Override
    public boolean hasPin(User user) {
        return pinRepo.findByUser(user).isPresent();
    }

    @Override
    @Transactional
    public void changePin(User user, String oldPin, String newPin) {
        // Verify old PIN first
        verifyPin(user, oldPin);

        // Setup new PIN
        setupPin(user, newPin);
    }

    private void validatePinFormat(String pin) {
        if (pin == null || !pin.matches("^\\d{6}$")) {
            throw new BusinessException("PIN phải gồm 6 chữ số");
        }

        // Reject simple patterns
        if (pin.matches("^(\\d)\\1{5}$")) { // 111111, 222222...
            throw new BusinessException("PIN quá đơn giản");
        }
        if (pin.equals("123456") || pin.equals("654321") || pin.equals("000000")) {
            throw new BusinessException("PIN quá đơn giản");
        }
    }
}