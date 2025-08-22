package com.spot4sale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookingDetailsDto(
        UUID id,
        UUID userId,
        UUID spotId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal totalPrice,
        StoreSummary store,
        SpotSummary spot
) {}
