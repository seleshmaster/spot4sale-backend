package com.spot4sale.dto;

import jakarta.validation.constraints.DecimalMin;

public record UpdateHostRequest(
        @DecimalMin("0.50") Double pricePerDay,
        Boolean available
) {}
