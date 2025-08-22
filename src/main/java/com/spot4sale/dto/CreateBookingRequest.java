
package com.spot4sale.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;
import java.time.temporal.ChronoUnit;

public record CreateBookingRequest(
  @NotNull UUID spotId,
  @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
  @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate
) {
  @AssertTrue(message = "startDate must be on or before endDate")
  public boolean isChronological() {
    if (startDate == null || endDate == null) return true;
    return !startDate.isAfter(endDate);
  }
  @AssertTrue(message = "Garage sale duration must be 1 or 2 days")
  public boolean isDurationOneOrTwoDays() {
    if (startDate == null || endDate == null) return true;
    long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    return days >= 1 && days <= 2;
  }
}
