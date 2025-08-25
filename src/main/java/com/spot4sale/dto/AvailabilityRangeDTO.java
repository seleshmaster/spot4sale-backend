// src/main/java/com/spot4sale/dto/AvailabilityRangeDTO.java
package com.spot4sale.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityRangeDTO(
        List<LocalDate> blackoutDays,
        List<Integer> openWeekdays,          // keep for backward compat (UI can ignore)
        List<SeasonDTO> seasons              // <-- add seasons here
) {}
