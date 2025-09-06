package com.spot4sale.dto;

import java.util.UUID;

public record StoreSummaryDTO(
        UUID id,
        String name,
        String address,
        String city,
        String zipCode,
        String thumbnail,
        Double averageRating   // new field for rating
) { }
