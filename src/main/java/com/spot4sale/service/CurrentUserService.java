package com.spot4sale.service;

import com.spot4sale.entity.User;
import com.spot4sale.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;



@Service
public class CurrentUserService {
    private final UserRepository users;

    public CurrentUserService(UserRepository users) { this.users = users; }

    public User require(Authentication auth) {
        if (auth == null) throw new IllegalStateException("Not authenticated");
        String email = (auth.getPrincipal() instanceof OidcUser o) ? o.getEmail() : auth.getName();
        if (email == null || email.isBlank()) throw new IllegalStateException("No email on principal");
        return users.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + email));
    }
}
