// src/main/java/com/spot4sale/controller/AuthController.java
package com.spot4sale.controller;

import com.spot4sale.dto.MeResponse;
import com.spot4sale.service.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUtils authService;

    public AuthController(AuthUtils authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        return authService.me(auth);
    }
}
