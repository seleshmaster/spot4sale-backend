// src/main/java/com/spot4sale/controller/AuthController.java
package com.spot4sale.controller;

import com.spot4sale.dto.MeResponse;
import com.spot4sale.entity.User;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.service.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUtils authService;
    private final UserRepository users;

    public AuthController(AuthUtils authService, UserRepository users) {
        this.users = users;
        this.authService = authService;
    }

//    @GetMapping("/me")
//    public MeResponse me(Authentication auth) {
//        return authService.me(auth);
//    }

    @GetMapping("/me")
    public MeResponse me(OAuth2AuthenticationToken auth) {
        return authService.getCurrentUser(auth);
    }

}
