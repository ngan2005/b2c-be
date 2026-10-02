package com.example.B2C.modules.user.repository;

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

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    /**
     * Loads user with role AND roles collection for multi-role authority resolution.
     * Must be used inside a transaction or with LAZY loading handled properly.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
        SELECT DISTINCT u FROM User u
        WHERE (:status IS NULL OR u.status = :status)
          AND (:roleCode IS NULL OR :roleCode IN (
              SELECT r.code FROM u.roles r
          ))
          AND (
            :keyword IS NULL
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<User> searchUsers(@Param("status") UserStatus status,
                           @Param("roleCode") String roleCode,
                           @Param("keyword") String keyword,
                           Pageable pageable);

    long countByStatus(UserStatus status);
}