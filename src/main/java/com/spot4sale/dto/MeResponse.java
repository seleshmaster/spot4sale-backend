// src/main/java/com/spot4sale/dto/MeResponse.java
package com.spot4sale.dto;

import java.util.List;

public record MeResponse(
        String name,
        String email,
        String role,
        List<String> authorities
) {}
