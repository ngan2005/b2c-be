package com.example.B2C.modules.user.repository;

import com.example.B2C.modules.user.entity.UserRole;
import com.example.B2C.modules.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserRole is a pure join entity and intentionally does not extend BaseEntity, so it
 * cannot extend BaseRepository (which is bounded by BaseEntity).
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.userId = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
