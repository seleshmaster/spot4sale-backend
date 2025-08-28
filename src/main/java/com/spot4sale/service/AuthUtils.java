
package com.spot4sale.service;

import com.spot4sale.dto.MeResponse;
import com.spot4sale.entity.User;
import com.spot4sale.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthUtils {
  private final UserRepository users;

  public String currentUserEmail(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }
    Object principal = auth.getPrincipal();
    if (principal instanceof OidcUser oidc) {
      if (oidc.getEmail() != null && !oidc.getEmail().isBlank()) return oidc.getEmail();
    }
    if (principal instanceof UserDetails ud) {
      return ud.getUsername();
    }
    String name = auth.getName();
    if (name != null && !name.isBlank()) return name;
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot resolve current user email");
  }

  public UUID currentUserId(Authentication auth) {
    String email = currentUserEmail(auth);
    return users.findByEmail(email).map(User::getId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public User currentUser(Authentication auth) {
    String email = currentUserEmail(auth);
    return users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public void requireRole(Authentication auth, String role) {
    User u = currentUser(auth);
    if (u.getRole() == null || !u.getRole().equalsIgnoreCase(role)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: requires role " + role);
    }
  }

    /** Resolve the current DB user by OIDC email or auth name. Throws if missing. */
    public static User requireUser(UserRepository users, Authentication auth) {
        if (auth == null) throw new IllegalStateException("Not authenticated");

        String email = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof OidcUser o) {
            email = o.getEmail();               // Google OIDC
        } else if (auth.getName() != null) {
            email = auth.getName();             // fallback
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Could not resolve user email from authentication");
        }

        final String someEmailSource =  email;
        return users.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + someEmailSource));
    }


    public MeResponse me(Authentication auth) {
        String email = auth.getName();
        User dbUser = users.findByEmail(email).orElse(null);

        String role = dbUser != null && dbUser.getRole() != null && !dbUser.getRole().isBlank()
                ? dbUser.getRole()
                : "USER";

        List<String> authorities = auth.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();

        return new MeResponse(
                dbUser != null && dbUser.getName()!=null ? dbUser.getName() : email,
                email,
                role,
                authorities
        );
    }

    /** Fetch or create the user, refresh role if necessary */
    @Transactional
    public MeResponse getCurrentUser(Authentication auth) {

        if (!(auth instanceof OAuth2AuthenticationToken)) {
            throw new IllegalArgumentException("Authentication is not OAuth2AuthenticationToken");
        }

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) auth;
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        // Step 1: Extract email from OAuth token
        String email = (String) attributes.getOrDefault("email", null);
        String name = (String) attributes.getOrDefault("name", "unknown");

        if (email == null) {
            // fallback if no email from OAuth
            email = "unknown";
        }
        // Try to find the user in DB
        User me = users.findByEmail(email)
                .orElseGet(() -> createUserFromOidc(auth)); // create if not exists

        // Convert authorities from Authentication to List<String>
        List<String> authorities = auth.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        // Return DTO for frontend
        return new MeResponse(
                me.getName(),
                me.getEmail(),
                me.getRole(),
                authorities
        );
    }


    /** Create new user from OIDC authentication */
    private User createUserFromOidc(Authentication auth) {
        User u = new User();
        u.setEmail(auth.getName()); // use principal email
        u.setName(auth.getName());  // or get from claims
        u.setRole("USER");
        // add default authorities if needed
        return users.save(u);
    }
}
