package com.spot4sale.dto;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

public record CreateHostRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 200) String address,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Pattern(
                regexp = "^[0-9A-Za-z -]{3,12}$",
                message = "zipCode must be 3–12 chars (letters/numbers/hyphen/space)"
        ) String zipCode,
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude,
        Integer cancellationCutoffHours,           // nullable; defaulted in service
        List<String> images,                        // multiple images
        String thumbnail,                           // single thumbnail
        Map<String, Object> characteristics        // flexible key-value attributes
) {}
