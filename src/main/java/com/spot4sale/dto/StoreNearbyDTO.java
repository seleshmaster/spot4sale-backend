
package com.spot4sale.dto;

import java.util.UUID;

public record StoreNearbyDTO(
  UUID id,
  String name,
  String address,
  String city,
  String zipCode,
  Double latitude,
  Double longitude,
  Double distanceMeters
) {}
