package com.example.B2C.modules.user.entity;

import com.example.B2C.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Multi-role collection backed by the {@code user_roles} join table. The legacy
     * single {@link #role} column is retained for backward compatibility - together
     * they cover both the V3 single-role path and the V4 join-table path used by
     * authorisation code via {@link #getRoles()} / {@link #hasRole(String)}.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Every {@link Role} associated with this user, deduplicated, covering both the
     * legacy single-role column ({@code users.role_id}) and the multi-role collection
     * mapped to {@code user_roles}. Ordering is not guaranteed.
     */
    public Set<Role> getRoles() {
        Set<Role> result = new HashSet<>();
        if (role != null) {
            result.add(role);
        }
        if (roles != null) {
            result.addAll(roles);
        }
        return result;
    }

    /**
     * Convenience predicate for authorisation checks. Matches the role code
     * case-insensitively against any role attached to the user.
     */
    public boolean hasRole(String code) {
        if (code == null) {
            return false;
        }
        return getRoles().stream()
                .anyMatch(r -> r.getCode() != null && r.getCode().equalsIgnoreCase(code));
    }
}