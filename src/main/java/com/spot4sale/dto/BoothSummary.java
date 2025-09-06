package com.spot4sale.dto;

import java.util.UUID;

public record BoothSummary(
        UUID id,
        UUID storeId,
        Double pricePerDay,
        Boolean available
) {}
