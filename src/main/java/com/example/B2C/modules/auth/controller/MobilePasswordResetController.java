package com.example.B2C.modules.auth.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.auth.dto.MobileForgotPasswordRequest;
import com.example.B2C.modules.auth.dto.MobileResetPasswordRequest;
import com.example.B2C.modules.auth.dto.MobileVerifyOtpRequest;
import com.example.B2C.modules.auth.dto.MobileVerifyOtpResponse;
import com.example.B2C.modules.auth.service.MobilePasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Password Reset", description = "Forgot-password flow using 6-digit OTP for Mobile App")
public class MobilePasswordResetController {

    private final MobilePasswordResetService mobilePasswordResetService;

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Gửi mã OTP 6 số qua email",
            description = "Gửi mã OTP 6 chữ số đến địa chỉ email đã đăng ký để đặt lại mật khẩu trên ứng dụng di động."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent (or silent success)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email không hợp lệ")
    })
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody MobileForgotPasswordRequest request) {
        mobilePasswordResetService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "Nếu email tồn tại trên hệ thống, mã xác thực OTP đã được gửi đến hộp thư của bạn.", null));
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Xác thực mã OTP 6 số",
            description = "Kiểm tra mã OTP 6 chữ số người dùng nhập. Nếu hợp lệ, trả về resetToken để chuyển sang màn hình đặt mật khẩu mới."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP hợp lệ, trả về resetToken"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mã OTP sai, hết hạn hoặc vượt quá số lần thử")
    })
    public ResponseEntity<ApiResponse<MobileVerifyOtpResponse>> verifyOtp(@Valid @RequestBody MobileVerifyOtpRequest request) {
        MobileVerifyOtpResponse response = mobilePasswordResetService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Xác thực OTP thành công", response));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Đặt lại mật khẩu mới bằng resetToken",
            description = "Cập nhật mật khẩu mới cho tài khoản sau khi đã xác thực OTP thành công."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mật khẩu không hợp lệ hoặc resetToken hết hạn")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody MobileResetPasswordRequest request) {
        mobilePasswordResetService.resetPassword(request.getEmail(), request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu mới thành công", null));
    }
}
