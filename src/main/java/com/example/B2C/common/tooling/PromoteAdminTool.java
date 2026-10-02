package com.example.B2C.common.tooling;

import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserRole;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Grants ADMIN to one account so the first administrator can be bootstrapped without a
 * public self-registration path. Only active in the {@code dev} and {@code local} profiles,
 * so this bean never exists in a deployed environment.
 *
 * <p>Credentials come from configuration ({@code bootstrap.admin.*}) rather than being
 * hardcoded, and are never logged.
 */
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class PromoteAdminTool implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Value("${bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${bootstrap.admin.password:}")
    private String adminPassword;

    @Value("${bootstrap.admin.full-name:}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("bootstrap.admin.email not set - skipping admin bootstrap.");
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "bootstrap.admin.email is set but bootstrap.admin.password is missing");
        }

        User target = userRepository.findByEmailWithRoles(adminEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "bootstrap.admin.email=" + adminEmail + " does not match any existing user. "
                                + "Register the account first, then re-run to promote it."));

        if (target.hasRole(Role.CODE_ADMIN)) {
            log.info("User {} is already an ADMIN - nothing to promote.", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByCode(Role.CODE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found in database"));

        // Set the primary role as well, so admin is reflected in every code path that still
        // reads users.role_id, and record it in the join table for the authority lookup.
        target.setRole(adminRole);
        if (adminFullName != null && !adminFullName.isBlank()) {
            target.setFullName(adminFullName);
        }
        target.setStatus(UserStatus.ACTIVE);
        UserRole userRole = UserRole.builder()
                .userId(target.getId())
                .roleId(adminRole.getId())
                .grantedAt(LocalDateTime.now())
                .build();

        userRepository.save(target);
        userRoleRepository.save(userRole);

        log.info("Promoted existing user {} to ADMIN.", adminEmail);
    }
}