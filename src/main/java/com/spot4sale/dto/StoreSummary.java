package com.spot4sale.dto;

import java.util.UUID;

public record StoreSummary(
        UUID id,
        String name,
        String address,
        String city,
        String zipCode
) {}
