package com.spot4sale.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot4sale.config.GoogleMapsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final GoogleMapsProperties props;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.maps.api-key}")
    private String apiKey;


    private static final String GEOCODE_URL =
            "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";

    public record LatLon(double lat, double lon) {}

    @Async
    public CompletableFuture<LatLon> fetchLatLon(String address) {
        String url = String.format(GEOCODE_URL, address.replace(" ", "+"), apiKey);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"OK".equals(response.get("status"))) {
            return CompletableFuture.completedFuture(new LatLon(0, 0)); // fallback
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results.isEmpty()) return CompletableFuture.completedFuture(new LatLon(0, 0));

        Map<String, Object> location = (Map<String, Object>) ((Map<String, Object>) results.get(0).get("geometry")).get("location");
        double lat = ((Number) location.get("lat")).doubleValue();
        double lon = ((Number) location.get("lng")).doubleValue();

        return CompletableFuture.completedFuture(new LatLon(lat, lon));
    }


    public double[] getLatLng(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                    .queryParam("address", address)
                    .queryParam("key", props.getApiKey())
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode location = results.get(0).get("geometry").get("location");
                double lat = location.get("lat").asDouble();
                double lng = location.get("lng").asDouble();
                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{0.0, 0.0}; // fallback
    }
}
