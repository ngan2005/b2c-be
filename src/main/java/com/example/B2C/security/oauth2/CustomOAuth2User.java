package com.example.B2C.security.oauth2;

import com.example.B2C.modules.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 principal returned by {@link CustomOAuth2UserService}. Carries the
 * resolved {@link User} so the success handler can issue a JWT without another
 * database lookup. The original {@code attributes} and {@code nameAttributeKey}
 * are passed through so Spring Security's default behaviour keeps working.
 */
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final User user;

    public CustomOAuth2User(User user,
                            Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
        this.user = user;
    }

    /**
     * Returns the email address of the user, mirroring {@code UserDetails} so
     * downstream code (e.g. JWT subject) can read it uniformly.
     */
    @Override
    public String getName() {
        return user.getEmail();
    }

    /**
     * Convenience: the {@code sub} claim as returned by the OAuth2 provider.
     * Falls back to the user id when the attribute is missing.
     */
    public String getProviderUserId() {
        Object sub = getAttributes().get("sub");
        return sub != null ? sub.toString() : String.valueOf(user.getId());
    }

    public static List<GrantedAuthority> noAuthorities() {
        return Collections.emptyList();
    }
}
