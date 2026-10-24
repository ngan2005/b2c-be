package com.example.B2C.security.oauth2;

import com.example.B2C.modules.user.entity.AuthProvider;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.RoleCode;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom {@link DefaultOAuth2UserService} that maps a Google / Facebook
 * callback into a local {@link User} row. Resolves the identity in three
 * steps:
 * <ol>
 *   <li>If a user already has this {@code (provider, sub)} pair, return it
 *       (re-login of an existing social account).</li>
 *   <li>Otherwise, look up a user by email. If found, link the social
 *       identity to the existing account and return it. The user can now
 *       sign in with either password or Google.</li>
 *   <li>Otherwise, create a new user as BUYER with the social identity and
 *       return it.</li>
 * </ol>
 */
@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    public static final String SESSION_ATTR_USER = "_b2c_oauth2_resolved_user";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Comma-separated list of emails that should be granted the {@link RoleCode#ADMIN}
     * role when they sign up via a social provider. Comparison against the OAuth2 email
     * is case-insensitive and trimmed.
     * <p>
     * The whitelist only applies to <em>newly created</em> users (case 3 in
     * {@link #resolveOrCreate}). Existing users are NEVER promoted by the whitelist
     * — to change the role of an existing user, use PromoteAdminTool or update the
     * database directly. This rule prevents an attacker from obtaining a BUYER
     * account by claiming control of an old, whitelisted email.
     */
    private final Set<String> adminEmailWhitelist;

    public CustomOAuth2UserService(UserRepository userRepository,
                                    RoleRepository roleRepository,
                                    @Value("${app.oauth2.admin-emails:}") String adminEmailsCsv) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.adminEmailWhitelist = parseWhitelist(adminEmailsCsv);
        if (!this.adminEmailWhitelist.isEmpty()) {
            log.info("OAuth2 admin email whitelist loaded ({} entries): {}",
                    this.adminEmailWhitelist.size(), this.adminEmailWhitelist);
        }
    }

    private static Set<String> parseWhitelist(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oauth2User, null);
    }

    /**
     * Visible for testing — accepts a pre-built {@link OAuth2User} (so unit
     * tests do not need to make real HTTP calls to Google) and runs the same
     * lookup/link/create logic as {@link #loadUser(OAuth2UserRequest)}.
     * Does NOT touch the session.
     */
    public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        return processOAuth2User(userRequest, oauth2User, null);
    }

    /**
     * Full version — accepts an optional {@link HttpServletRequest} to stash the
     * resolved {@link User} into the session. When Google uses the
     * {@code openid} scope, Spring Security wraps our {@link CustomOAuth2User}
     * in a {@code DefaultOidcUser} after {@link #loadUser} returns, discarding
     * our custom attributes. Storing the User in the session lets
     * {@link OAuth2SuccessHandler} retrieve it from the {@link OidcUser} even
     * when the principal type changes.
     */
    public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User,
                                         HttpServletRequest request) {
        AuthProvider provider = resolveProvider(userRequest);
        String providerUserId = extractProviderUserId(oauth2User, provider);
        String email = extractEmail(oauth2User);
        String fullName = extractFullName(oauth2User, email);
        String avatarUrl = extractAttribute(oauth2User, "picture");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "OAuth2 provider did not return an email address; cannot proceed");
        }

        User user = resolveOrCreate(provider, providerUserId, email, fullName, avatarUrl);

        // Stash the resolved User in the session so OAuth2SuccessHandler can find it
        // even when the principal is later wrapped in DefaultOidcUser (openid scope).
        if (request != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_ATTR_USER, user);
            log.debug("OAuth2 resolved user {} stored in session {}", user.getEmail(), session.getId());
        }

        return new CustomOAuth2User(
                user,
                CustomOAuth2User.noAuthorities(),
                oauth2User.getAttributes(),
                "sub");
    }

    private AuthProvider resolveProvider(OAuth2UserRequest req) {
        String regId = req.getClientRegistration().getRegistrationId();
        try {
            return AuthProvider.valueOf(regId.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown OAuth2 client registration: {}", regId);
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + regId);
        }
    }

    private String extractProviderUserId(OAuth2User oauth2User, AuthProvider provider) {
        // Google and Facebook both return a stable "sub"/"id" claim respectively.
        Object sub = oauth2User.getAttributes().get("sub");
        if (sub != null) {
            return sub.toString();
        }
        Object id = oauth2User.getAttributes().get("id");
        if (id != null) {
            return id.toString();
        }
        throw new OAuth2AuthenticationException(
                "OAuth2 provider " + provider + " did not return a stable user id");
    }

    private String extractEmail(OAuth2User oauth2User) {
        Object email = oauth2User.getAttributes().get("email");
        return email != null ? email.toString() : null;
    }

    private String extractFullName(OAuth2User oauth2User, String fallbackEmail) {
        Object name = oauth2User.getAttributes().get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        Object given = oauth2User.getAttributes().get("given_name");
        Object family = oauth2User.getAttributes().get("family_name");
        if (given != null || family != null) {
            return ((given != null) ? given : "") + " " + ((family != null) ? family : "");
        }
        return fallbackEmail;
    }

    private String extractAttribute(OAuth2User oauth2User, String key) {
        Object value = oauth2User.getAttributes().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Resolve a User row for the given social identity, linking by email if
     * possible, or creating a new BUYER account otherwise.
     */
    private User resolveOrCreate(AuthProvider provider, String providerUserId,
                                 String email, String fullName, String avatarUrl) {
        // 1. Existing social identity?
        Optional<User> bySocial = userRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (bySocial.isPresent()) {
            log.info("OAuth2 re-login for existing social user {}", email);
            return initializeUserAssociations(bySocial.get());
        }

        // 2. Existing local user with this email? Link the social identity.
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            log.info("Linking OAuth2 identity {} to existing local user {}", providerUserId, email);
            existing.setProvider(provider);
            existing.setProviderUserId(providerUserId);
            existing.setProviderLinkedAt(LocalDateTime.now());
            if (avatarUrl != null && existing.getAvatarUrl() == null) {
                existing.setAvatarUrl(avatarUrl);
            }
            return initializeUserAssociations(userRepository.save(existing));
        }

        // 3. Create a new user. Default role is BUYER; if the email is on the
        // admin whitelist, grant ADMIN instead. Whitelist promotion only
        // happens here (new user); existing users are NEVER promoted by the
        // whitelist (see adminEmailWhitelist Javadoc).
        RoleCode assignedRoleCode = isAdminEmail(email) ? RoleCode.ADMIN : RoleCode.BUYER;
        log.info("Creating new user from OAuth2 identity {} ({}) with role {}",
                providerUserId, email, assignedRoleCode);
        Role assignedRole = roleRepository.findByCode(assignedRoleCode.name())
                .orElseThrow(() -> new IllegalStateException(
                        assignedRoleCode + " role not found in database"));
        User newUser = User.builder()
                .role(assignedRole)
                .email(email)
                .passwordHash(null)
                .fullName(fullName != null ? fullName : email)
                .avatarUrl(avatarUrl)
                .provider(provider)
                .providerUserId(providerUserId)
                .providerLinkedAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                // Populating User.roles lets the @OneToMany CascadeType.PERSIST
                // write the user_roles join row automatically. Do NOT also call
                // userRoleRepository.save() here — that would violate the
                // (user_id, role_id) primary key.
                .roles(new HashSet<>(Set.of(assignedRole)))
                .build();
        User saved = userRepository.save(newUser);
        return initializeUserAssociations(saved);
    }

    /**
     * Force-initialize any LAZY associations on the User that downstream code
     * (notably {@code OAuth2SuccessHandler.handleLoginSuccess} which reads
     * {@code user.getRole().getCode()}) accesses after this {@code @Transactional}
     * method returns. The Hibernate session is closed at the boundary; if we
     * don't touch the proxy here, callers will hit
     * {@code LazyInitializationException}.
     */
    private User initializeUserAssociations(User user) {
        if (user.getRole() != null) {
            // Triggers Hibernate proxy initialization; the value is discarded.
            user.getRole().getCode();
        }
        if (user.getRoles() != null) {
            // .size() forces the SELECT for the @OneToMany Set.
            user.getRoles().size();
        }
        return user;
    }

    /**
     * Case-insensitive membership check against the configured admin email
     * whitelist. Returns {@code false} when the whitelist is empty (the
     * common dev case where admin promotion is done via BootstrapAdminTool).
     */
    private boolean isAdminEmail(String email) {
        if (email == null || adminEmailWhitelist.isEmpty()) {
            return false;
        }
        return adminEmailWhitelist.contains(email.trim().toLowerCase(java.util.Locale.ROOT));
    }
}
