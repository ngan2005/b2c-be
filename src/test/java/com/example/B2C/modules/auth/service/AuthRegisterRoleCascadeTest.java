package com.example.B2C.modules.auth.service;

import com.example.B2C.modules.auth.dto.RegisterRequest;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.user.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that registering a new user creates a row in the user_roles join
 * table via JPA cascade. The @OneToMany mapping on User.roles has
 * CascadeType.PERSIST, so this must work without an explicit
 * userRoleRepository.save call.
 */
@SpringBootTest
@Transactional
class AuthRegisterRoleCascadeTest {

    @Autowired private AuthServiceImpl authService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private RoleRepository roleRepository;

    @org.junit.jupiter.api.BeforeEach
    @Transactional
    void seedRoles() {
        if (roleRepository.findByCode("BUYER").isEmpty()) {
            roleRepository.save(Role.builder().code("BUYER").name("Buyer").build());
        }
    }

    @Test
    @Transactional
    @DisplayName("register() creates a user_roles join row for the BUYER role")
    void register_persistsUserRoleJoinRow() {
        String email = "cascade-" + UUID.randomUUID() + "@example.com";
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setFullName("Cascade User");
        req.setPhone("09" + (System.currentTimeMillis() % 1_000_000_000L));

        var response = authService.register(req);

        assertNotNull(response.getUserId());
        User saved = userRepository.findById(response.getUserId()).orElseThrow();
        // The DTO and the join table both reflect BUYER
        assertEquals("BUYER", response.getRole());
        // The User.roles Set must contain BUYER for downstream code that
        // resolves authorities via the join table.
        assertTrue(saved.getRoles().stream()
                        .anyMatch(r -> "BUYER".equals(r.getCode())),
                "user must have BUYER in the roles collection");

        // And the join row actually exists in the database.
        assertTrue(userRoleRepository.existsByUserIdAndRoleId(
                        saved.getId(), saved.getRole().getId()),
                "user_roles join row must exist for the new BUYER user");
    }
}
