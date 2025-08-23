package com.spot4sale.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityRangeDTO(List<LocalDate> blackouts,
                                   List<Integer> openWeekdays /*1..7, optional*/) {}