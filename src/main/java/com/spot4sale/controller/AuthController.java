
package com.spot4sale.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @GetMapping("/me")
  public ResponseEntity<?> me(Authentication auth){
    if (auth == null) return ResponseEntity.status(401).body(Map.of("error","Unauthorized"));
    return ResponseEntity.ok(Map.of("name", auth.getName(), "authorities", auth.getAuthorities()));
  }

}
