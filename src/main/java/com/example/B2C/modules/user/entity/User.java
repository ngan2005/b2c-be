package com.example.B2C.modules.user.entity;

import com.example.B2C.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    /**
     * BCrypt hash of the user's password. Nullable for social-only sign-ups
     * (Google, Facebook, ...).
     */
    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * How this user authenticates. {@code LOCAL} for the legacy email/password
     * path, {@code GOOGLE} / {@code FACEBOOK} for OAuth2-only sign-ups.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    /**
     * Stable id returned by the OAuth2 provider (e.g. Google "sub" claim).
     * Null for local users.
     */
    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    /**
     * Timestamp the social identity was linked to this user account. Null
     * until the first social login completes.
     */
    @Column(name = "provider_linked_at")
    private LocalDateTime providerLinkedAt;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    /**
     * Cloudinary public_id of the uploaded avatar. Populated by
     * {@code UserServiceImpl} when the user uploads a replacement avatar,
     * so the previous asset can be destroyed from the storage backend
     * instead of being orphaned. Null for rows that pre-date this column
     * or whose avatar came from an OAuth2 provider.
     */
    @Column(name = "avatar_public_id")
    private String avatarPublicId;

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
     * Every {@link Role} attached to this user via the {@code user_roles}
     * join table (the authoritative source since V21).
     *
     * <p>For single-role enforcement (ADMIN / SELLER / BUYER — one per user),
     * callers should prefer {@link #getPrimaryRole()} or
     * {@link com.example.B2C.modules.user.util.UserRoleResolver#getHighestRoleCode}.
     *
     * <p>Ordering is not guaranteed.
     */
    public Set<Role> getRoles() {
        // Primary authoritative source: join table user_roles.
        // The legacy single-role column (users.role_id) is no longer written
        // for multi-role scenarios; it is kept only for compatibility and
        // synchronised by V21__enforce_single_role.sql.
        //
        // IMPORTANT: this returns the LIVE collection, not a defensive copy.
        // Callers legitimately mutate it right after building a User
        // (e.g. `user.getRoles().add(buyerRole)`) and then read the roles
        // back to mint the JWT. Returning a copy silently discarded those
        // mutations, leaving the entity with no authorities and the issued
        // token without any role.
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }

    /**
     * Returns the single authoritative {@link Role} for this user by applying
     * the priority rule ADMIN &gt; SELLER &gt; BUYER across the join-table
     * roles. Returns {@code null} only if the user has no role in
     * {@code user_roles} at all (the legacy {@code users.role_id} column is
     * used as a last-resort fallback for orphan accounts).
     *
     * <p>Use this method instead of {@code getRoles()} when you need exactly
     * one role — notably in {@link com.example.B2C.security.CustomUserDetails}
     * to build the JWT authority set.
     */
    public Role getPrimaryRole() {
        // Try join table first (authoritative since V21).
        Set<Role> fromJoin = this.roles;
        if (fromJoin != null && !fromJoin.isEmpty()) {
            return fromJoin.stream()
                    .filter(r -> r != null && r.getCode() != null)
                    .max(Comparator.comparingInt(r -> priorityOf(r.getCode())))
                    .orElse(null);
        }
        // Fallback: legacy single-role column for orphan accounts.
        return this.role;
    }

    private static int priorityOf(String code) {
        if (code == null) return 0;
        return switch (code.toUpperCase()) {
            case "ADMIN" -> 30;
            case "SELLER" -> 20;
            case "BUYER" -> 10;
            default -> 0;
        };
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