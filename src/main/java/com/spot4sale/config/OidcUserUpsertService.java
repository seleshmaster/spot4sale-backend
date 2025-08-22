// src/main/java/com/spot4sale/config/OidcUserUpsertService.java
package com.spot4sale.config;

import com.spot4sale.entity.User;
import com.spot4sale.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OidcUserUpsertService extends OidcUserService {
    private final UserRepository users;

    public OidcUserUpsertService(UserRepository users) { this.users = users; }

    @Override
    public OidcUser loadUser(OidcUserRequest req) {
        OidcUser oidc = super.loadUser(req);

        String email = oidc.getEmail();
        String name  = oidc.getFullName() != null ? oidc.getFullName() : email;

        users.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setName(name);
            u.setRole("USER");
            u.setAuthProvider("GOOGLE");
            return users.save(u);
        });

        // Merge authorities
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidc.getAuthorities());
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new DefaultOidcUser(mappedAuthorities, oidc.getIdToken(), oidc.getUserInfo());
    }
}
