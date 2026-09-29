package com.example.B2C.security;

import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Authorities come from the join table (user_roles) plus the legacy primary role
        // (users.role_id). A HashSet deduplicates: after approval a user holds BUYER in both
        // places and must not end up with a doubled authority.
        Set<GrantedAuthority> authorities = new HashSet<>();
        addAuthority(authorities, user.getRole());
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> addAuthority(authorities, role));
        }
        return authorities;
    }

    private void addAuthority(Set<GrantedAuthority> authorities, Role role) {
        if (role == null || role.getCode() == null) {
            return;
        }
        String code = role.getCode();
        authorities.add(new SimpleGrantedAuthority(
                code.startsWith("ROLE_") ? code : "ROLE_" + code));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.LOCKED && user.getStatus() != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null;
    }
}
