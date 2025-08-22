package com.spot4sale.dto;

import java.util.UUID;

public record SpotSummary(
        UUID id,
        UUID storeId,
        Double pricePerDay,
        Boolean available
) {}
