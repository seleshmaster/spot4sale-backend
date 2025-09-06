package com.spot4sale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@EnableAsync
@SpringBootApplication
public class VendorHubApplication {

    @GetMapping("/api/me/roles")
    public Collection<? extends GrantedAuthority> roles(Authentication auth) {
        return auth.getAuthorities();
    }

  public static void main(String[] args) { SpringApplication.run(VendorHubApplication.class, args); }
}