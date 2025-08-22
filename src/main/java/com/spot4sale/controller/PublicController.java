package com.spot4sale.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String,Object> health() {
        return Map.of("status", "ok", "time", Instant.now().toString());
    }
}

@RestController
class RootRedirectController {
    @GetMapping("/")
    public ResponseEntity<Void> root() {
        return ResponseEntity.status(302).location(URI.create("/swagger-ui.html")).build();
    }
}
