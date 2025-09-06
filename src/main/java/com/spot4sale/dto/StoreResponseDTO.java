package com.spot4sale.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot4sale.entity.Host;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record StoreResponseDTO(
        UUID id,
        String name,
        String description,
        String address,
        String city,
        String zipCode,
        Double latitude,
        Double longitude,
        Integer cancellationCutoffHours,
        List<String> images,
        String thumbnail,
        Map<String, Object> characteristics
) {

    public static StoreResponseDTO from(Host store) {

        Map<String, Object> charMap = null;

        if (store.getCharacteristics() != null) {
            try {
                charMap = new ObjectMapper()
                        .readValue(
                                store.getCharacteristics(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                        );
            } catch (Exception e) {
                charMap = Map.of(); // fallback to empty map
            }
        }
        List<String> images = store.getImages() != null ? Arrays.asList(store.getImages()) : List.of();

        return new StoreResponseDTO(
                store.getId(),
                store.getName(),
                store.getDescription(),
                store.getAddress(),
                store.getCity(),
                store.getZipCode(),
                store.getLatitude(),
                store.getLongitude(),
                store.getCancellationCutoffHours(),
                images,
                store.getThumbnail(),
                charMap // assuming this is a Map<String,Object> mapped from JSON
        );
    }

}
