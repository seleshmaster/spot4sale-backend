
package com.spot4sale.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateReviewRequest(
  @NotNull UUID targetId,
  @NotBlank @Pattern(regexp = "STORE|SELLER", message = "targetType must be STORE or SELLER") String targetType,
  @NotNull @Min(1) @Max(5) Integer rating,
  @Size(max = 2000) String comment
) {}
