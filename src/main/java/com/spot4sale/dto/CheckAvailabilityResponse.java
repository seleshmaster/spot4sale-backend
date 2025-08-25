package com.spot4sale.dto;

import java.math.BigDecimal;

public record CheckAvailabilityResponse(
        boolean ok,
        String reason,            // null if ok
        BigDecimal estimatedTotal // may be null if not applicable
) {}
