package com.example.B2C.modules.user.repository;

import com.example.B2C.modules.user.entity.AuthProvider;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    /**
     * Find a user by the (provider, providerUserId) pair returned from
     * an OAuth2 callback. Returns empty if the social identity has not been
     * linked yet — the caller should then attempt email-based link or create
     * a new account.
     */
    Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    /**
     * Loads user with role AND roles collection for multi-role authority resolution.
     * Must be used inside a transaction or with LAZY loading handled properly.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.role LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

@Query(value = """
    SELECT u.id, u.avatar_public_id, u.avatar_url, u.created_at, u.date_of_birth, u.deleted_at,
           u.email, u.email_verified_at, u.full_name, u.gender, u.last_login_at,
           u.password_hash, u.phone, u.phone_verified_at, u.provider, u.provider_linked_at,
           u.provider_user_id, u.role_id, u.status, u.updated_at
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN role r ON r.id = u.role_id
    LEFT JOIN role r2 ON r2.id = ur.role_id
    WHERE (:status IS NULL OR u.status = CAST(:status AS VARCHAR))
      AND (:roleCode IS NULL OR r.code = :roleCode OR r2.code = :roleCode)
      AND (:keyword IS NULL
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR (u.full_name IS NOT NULL AND LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')))
           OR (u.phone IS NOT NULL AND u.phone LIKE CONCAT('%', :keyword, '%'))
      )
    GROUP BY u.id, u.avatar_public_id, u.avatar_url, u.created_at, u.date_of_birth, u.deleted_at,
             u.email, u.email_verified_at, u.full_name, u.gender, u.last_login_at,
             u.password_hash, u.phone, u.phone_verified_at, u.provider, u.provider_linked_at,
             u.provider_user_id, u.role_id, u.status, u.updated_at
    ORDER BY u.created_at DESC
    """,
    countQuery = """
    SELECT COUNT(DISTINCT u.id)
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    LEFT JOIN role r ON r.id = u.role_id
    LEFT JOIN role r2 ON r2.id = ur.role_id
    WHERE (:status IS NULL OR u.status = CAST(:status AS VARCHAR))
      AND (:roleCode IS NULL OR r.code = :roleCode OR r2.code = :roleCode)
      AND (:keyword IS NULL
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR (u.full_name IS NOT NULL AND LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')))
           OR (u.phone IS NOT NULL AND u.phone LIKE CONCAT('%', :keyword, '%'))
      )
    """,
    nativeQuery = true)
    Page<User> searchUsers(@Param("status") String status,
                           @Param("roleCode") String roleCode,
                           @Param("keyword") String keyword,
                           Pageable pageable);

    long countByStatus(UserStatus status);
}