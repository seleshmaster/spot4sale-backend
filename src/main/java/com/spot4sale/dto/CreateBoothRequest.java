
package com.spot4sale.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateBoothRequest(
        @NotNull UUID storeId,
        @NotNull @DecimalMin("0.50") Double pricePerDay,
        @NotNull Boolean available
) {}