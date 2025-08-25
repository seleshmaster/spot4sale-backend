package com.spot4sale.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SeasonDTO(UUID id, LocalDate startDate, LocalDate endDate, List<Integer> openWeekdays, String note) {}