package com.example.B2C.modules.user.util;

import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;

import java.util.List;

/**
 * Resolves the "primary" role code for a {@link User} from the
 * multi-role join table ({@code user_roles}) rather than the legacy
 * single {@code users.role_id} column.
 *
 * <p>The single column is no longer kept in sync for every promotion
 * path (only some code paths call {@code user.setRole(...)}), so reading
 * from it gave users the wrong role in OAuth/Auth/Profile responses.
 * The join table is the authoritative source: every code path writes
 * there, and {@code @PreAuthorize("hasRole('SELLER')")} already reads
 * from it via {@code CustomUserDetails}.
 *
 * <p>Priority is {@code ADMIN > SELLER > BUYER} — the highest privilege
 * the user holds is what we surface to the frontend for navigation and
 * to the auth response for UI rendering.
 */
public final class UserRoleResolver {

    private UserRoleResolver() {}

    /**
     * Priority map: higher number wins. Unknown codes fall back to
     * priority 0 (so a future role does not outrank existing ones).
     */
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
     * Returns the highest-priority role code for the user, reading only from
     * the join table ({@code user_roles}) — the authoritative single-role source
     * since V21. Returns {@code null} if the user has no role assigned.
     *
     * <p>Priority is {@code ADMIN > SELLER > BUYER}. The logic delegates to
     * {@link User#getPrimaryRole()} which performs the same selection, so this
     * method is equivalent to calling {@code user.getPrimaryRole()?.code}
     * directly. It is retained for backward compatibility with existing callers.
     */
    public static String getHighestRoleCode(User user) {
        if (user == null) return null;
        Role primary = user.getPrimaryRole();
        if (primary == null || primary.getCode() == null) return null;
        return primary.getCode();
    }

    /**
     * Returns all role codes assigned to the user via the join table
     * ({@code user_roles}), ordered by priority descending
     * (ADMIN &gt; SELLER &gt; BUYER). Returns an empty list if the user
     * has no roles assigned.
     *
     * <p>Use this method when you need the complete set of roles — e.g.
     * to populate {@link com.example.B2C.modules.auth.dto.AuthResponse#roles}.
     */
    public static List<String> getAllRoleCodes(User user) {
        if (user == null) return List.of();
        return user.getRoles().stream()
                .filter(r -> r != null && r.getCode() != null)
                .sorted((a, b) -> Integer.compare(priorityOf(b.getCode()), priorityOf(a.getCode())))
                .map(Role::getCode)
                .toList();
    }
}
