package com.spot4sale.dto;

import java.util.UUID;

public interface StoreNearbyDTO {
    UUID getId();
    String getName();
    String getAddress();
    String getCity();
    String getZipCode();
    Double getLatitude();
    Double getLongitude();
    Double getDistanceMeters();
}
