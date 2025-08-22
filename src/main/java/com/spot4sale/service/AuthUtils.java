
package com.spot4sale.service;

import com.spot4sale.entity.User;
import com.spot4sale.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

}
