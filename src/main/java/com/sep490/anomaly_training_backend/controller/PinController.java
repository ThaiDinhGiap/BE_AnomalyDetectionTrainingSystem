package com.sep490.anomaly_training_backend.controller;

import com.sep490.anomaly_training_backend.dto.request.ChangePinRequest;
import com.sep490.anomaly_training_backend.dto.request.SetupPinRequest;
import com.sep490.anomaly_training_backend.dto.response.ApiResponse;
import com.sep490.anomaly_training_backend.dto.response.PinStatusResponse;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserSignaturePinRepository;
import com.sep490.anomaly_training_backend.service.approval.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pin")
@RequiredArgsConstructor
@Tag(name = "PIN Management", description = "Quản lý PIN chữ ký số")
public class PinController {

    private final PinService pinService;
    private final UserSignaturePinRepository pinRepository;

    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái PIN của user hiện tại")
    public ResponseEntity<ApiResponse<PinStatusResponse>> getPinStatus(
            @AuthenticationPrincipal User currentUser) {

        PinStatusResponse response = pinRepository.findByUser(currentUser)
                .map(pin -> PinStatusResponse.builder()
                        .hasPin(true)
                        .isLocked(pin.isLocked())
                        .isExpired(pin.isExpired())
                        .expiresAt(pin.getExpiresAt())
                        .lockedUntil(pin.getLockedUntil())
                        .failedAttempts(pin.getFailedAttempts())
                        .build())
                .orElse(PinStatusResponse.builder()
                        .hasPin(false)
                        .isLocked(false)
                        .isExpired(false)
                        .build());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/setup")
    @Operation(summary = "Thiết lập PIN lần đầu")
    public ResponseEntity<ApiResponse<Void>> setupPin(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SetupPinRequest request) {

        if (pinService.hasPin(currentUser)) {
            throw new BusinessException("Bạn đã có PIN. Vui lòng sử dụng chức năng đổi PIN.");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new BusinessException("PIN xác nhận không khớp");
        }

        pinService.setupPin(currentUser, request.getPin());

        return ResponseEntity.ok(ApiResponse.success("Thiết lập PIN thành công", null));
    }

    @PostMapping("/change")
    @Operation(summary = "Đổi PIN")
    public ResponseEntity<ApiResponse<Void>> changePin(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePinRequest request) {

        if (!request.getNewPin().equals(request.getConfirmNewPin())) {
            throw new BusinessException("PIN xác nhận không khớp");
        }

        pinService.changePin(currentUser, request.getOldPin(), request.getNewPin());

        return ResponseEntity.ok(ApiResponse.success("Đổi PIN thành công", null));
    }

    @PostMapping("/verify")
    @Operation(summary = "Xác thực PIN (dùng để test)")
    public ResponseEntity<ApiResponse<Void>> verifyPin(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String pin) {

        pinService.verifyPin(currentUser, pin);

        return ResponseEntity.ok(ApiResponse.success("PIN hợp lệ", null));
    }
}