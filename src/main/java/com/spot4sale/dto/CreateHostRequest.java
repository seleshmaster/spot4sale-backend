package com.spot4sale.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        Map<String, Object> characteristics,       // flexible key-value attributes

        // --- New fields ---
        Double defaultPrice,                        // nullable
        List<String> defaultAmenities,              // list of amenities
        Integer maxBooths,                          // nullable
        Map<String, Object> operatingHours,        // JSON key-value for operating hours
        String contactEmail,                        // nullable
        String contactPhone,                        // nullable
        List<String> tags,                          // list of tags
        Integer footTrafficEstimate,                // nullable
        String cancellationPolicy,                  // nullable
        Integer bookingWindowDays,                  // nullable
        Boolean active,                             // nullable; defaults to true if not provided
        List<UUID> amenityIds,                      // link by ID

        // --- New: resolve by name ---
        String hostTypeName,
        String hostCategoryName
) {}
