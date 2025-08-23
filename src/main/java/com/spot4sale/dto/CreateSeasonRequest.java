package com.spot4sale.dto;

import java.time.LocalDate;

public record CreateSeasonRequest(LocalDate startDate, LocalDate endDate, int[] openWeekdays, String note) {}
