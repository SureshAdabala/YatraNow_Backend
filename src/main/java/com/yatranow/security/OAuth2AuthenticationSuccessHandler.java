package com.yatranow.security;

import com.yatranow.entity.User;
import com.yatranow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handles successful OAuth2 logins from Google and GitHub.
 * – Always registers/finds users with role = USER only.
 * – Admins are registered manually (never via OAuth2).
 * – Wraps all DB/JWT logic in try-catch to prevent unhandled 500s.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${oauth2.authorized-redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // "google" or "github"

            // Extract email and name based on the provider
            String email = extractEmail(oAuth2User, registrationId);
            String name  = extractName(oAuth2User, registrationId);

            if (email == null || email.isBlank()) {
                log.error("OAuth2 login failed: could not retrieve email from provider '{}'", registrationId);
                redirectToError(request, response, "Email not available from OAuth2 provider");
                return;
            }

            log.info("OAuth2 login attempt: email='{}', provider='{}'", email, registrationId);

            // Find existing user or auto-register as USER (never ADMIN/OWNER via OAuth2)
            User user = userRepository.findByEmail(email)
                    .map(existingUser -> updateProviderIfNeeded(existingUser, registrationId))
                    .orElseGet(() -> createOAuth2User(email, name, registrationId.toUpperCase()));

            // Generate JWT token
            String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());

            // Build redirect URL with token info as query params
            String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("token", jwt)
                    .queryParam("role",  user.getRole())
                    .queryParam("name",  user.getName() != null ? user.getName() : "User")
                    .queryParam("email", user.getEmail())
                    .queryParam("id",    user.getId())
                    .build().toUriString();

            log.info("OAuth2 login successful for '{}' via '{}'. Redirecting.", email, registrationId);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception ex) {
            log.error("OAuth2 authentication success handler error: {}", ex.getMessage(), ex);
            redirectToError(request, response, "Authentication processing failed. Please try again.");
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * If an existing user was created before OAuth2 was added (authProvider = null),
     * update their authProvider field so future lookups are consistent.
     */
    private User updateProviderIfNeeded(User user, String registrationId) {
        if (user.getAuthProvider() == null || user.getAuthProvider().equals("LOCAL")) {
            user.setAuthProvider(registrationId.toUpperCase());
            return userRepository.save(user);
        }
        return user;
    }

    /**
     * Auto-register a brand-new OAuth2 user with role = USER only.
     * Admins/Owners must register manually.
     */
    private User createOAuth2User(String email, String name, String provider) {
        log.info("Auto-registering new OAuth2 user: email='{}', provider='{}'", email, provider);
        User user = new User();
        user.setEmail(email);
        user.setName(name != null ? name : email.split("@")[0]);
        user.setPassword(null);          // No password for OAuth2 users
        user.setMobile(null);            // No mobile for OAuth2 users
        user.setRole("USER");            // ← Always USER; Admin registers manually
        user.setIsBlocked(false);
        user.setAuthProvider(provider);
        return userRepository.save(user);
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return oAuth2User.getAttribute("email");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                // GitHub may not expose email publicly — fallback to login@github.oauth
                String login = oAuth2User.getAttribute("login");
                if (login != null) {
                    return login + "@github.oauth";
                }
            }
            return email;
        }
        return null;
    }

    private String extractName(OAuth2User oAuth2User, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return oAuth2User.getAttribute("name");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            String name = oAuth2User.getAttribute("name");
            if (name == null) {
                name = oAuth2User.getAttribute("login"); // fallback to GitHub username
            }
            return name;
        }
        return null;
    }

    /**
     * Redirects to the frontend login page with an error message in the URL.
     */
    private void redirectToError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String errorMessage) throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", errorMessage)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}
