package com.example.B2C.modules.user.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.user.dto.UpdateUserRoleRequest;
import com.example.B2C.modules.user.dto.UpdateUserStatusRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin endpoints for searching and managing platform users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List users (filter by status / role / keyword, paginated)")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileDto>>> list(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsersForAdmin(status, role, q, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user detail")
    public ResponseEntity<ApiResponse<UserProfileDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserForAdmin(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change user status (ACTIVE/LOCKED/BANNED)")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserStatus status = UserStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                userService.updateUserStatusForAdmin(id, status)));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change user primary role (BUYER/SELLER/ADMIN)")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role updated",
                userService.updateUserRoleForAdmin(id, request.getRole())));
    }
}