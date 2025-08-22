
package com.spot4sale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import jakarta.servlet.http.HttpServletResponse;



@Configuration
public class SecurityConfig {

    private final OidcUserUpsertService oidcUserUpsertService;
    public SecurityConfig(OidcUserUpsertService oidcUserUpsertService) {
        this.oidcUserUpsertService = oidcUserUpsertService;
    }


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      //  String frontendBase = System.getenv().getOrDefault("FRONTEND_BASE_URL", "http://localhost:4200");
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/api/payments/webhook",
                                "/actuator/health", "/v3/api-docs/**",
                                "/swagger-ui.html", "/swagger-ui/**","/oauth2/**", "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/spots").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/spots/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings").authenticated()
                        .requestMatchers("/api/owner/**").authenticated()
                        .anyRequest().authenticated())// <-- add this
                .oauth2Login(o -> o
                        .userInfoEndpoint(u -> u.oidcUserService(oidcUserUpsertService))
                        .successHandler((req, res, auth) -> {
                            // your RETURN_TO cookie redirect code (keep as is)
                            String frontendBase = System.getenv().getOrDefault("FRONTEND_BASE_URL", "http://localhost:4200");
                            String returnTo = "/search";
                            var cookies = req.getCookies();
                            if (cookies != null) {
                                for (var c : cookies) {
                                    if ("RETURN_TO".equals(c.getName())) {
                                        returnTo = java.net.URLDecoder.decode(c.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                                        var del = new jakarta.servlet.http.Cookie("RETURN_TO", "");
                                        del.setPath("/");
                                        del.setMaxAge(0);
                                        res.addCookie(del);
                                        break;
                                    }
                                }
                            }
                            if (!returnTo.startsWith("/")) returnTo = "/search";
                            res.sendRedirect(frontendBase + returnTo);
                        })
                )
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")
                ))
                .logout(l -> l
                        .logoutUrl("/logout")                 // POST /logout
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((req, res, auth) -> {
                            // keep it simple: return 200 so the SPA controls navigation
                            res.setStatus(HttpServletResponse.SC_OK);
                        })
                );
        return http.build();
    }
}
