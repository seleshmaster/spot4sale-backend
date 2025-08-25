package com.spot4sale.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CheckAvailabilityRequest(
        UUID spotId,
        LocalDate startDate,
        LocalDate endDate
) {}
