package com.fiap.sus.network.modules.health_unit.service;

import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.fiap.sus.network.core.exception.ExternalServiceException;
import com.fiap.sus.network.shared.util.DistanceUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fiap.sus.network.core.config.AppConfigProperties;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final AppConfigProperties appConfig;
    private final ObjectMapper objectMapper;

    public GeocodedLocation geocode(String address) {
        log.info("Fetching coordinates and location info for address: {}", address);
        try {
            String baseUrl = appConfig.getGeocoding().getUrl();
            String url = baseUrl + "?q=" + address.replace(" ", "+") + "&format=json&limit=1&addressdetails=1";
            String response = restTemplate.getForObject(url, String.class);

            if (response != null && !response.equals("[]")) {
                JsonNode root = objectMapper.readTree(response);
                if (root.isArray() && !root.isEmpty()) {
                    JsonNode result = root.get(0);
                    double lat = result.get("lat").asDouble();
                    double lon = result.get("lon").asDouble();
                    
                    JsonNode addrNode = result.get("address");
                    String city = extractCity(addrNode);
                    String state = addrNode.has("state") ? addrNode.get("state").asText() : null;

                    return new GeocodedLocation(lat, lon, city, state);
                }
            }
        } catch (Exception e) {
            log.error("Error geocoding", e);
            throw new ExternalServiceException("Failed to fetch coordinates from external service", e);
        }
        return new GeocodedLocation(0.0, 0.0, null, null);
    }

    private String extractCity(JsonNode addrNode) {
        if (addrNode == null) return null;
        if (addrNode.has("city")) return addrNode.get("city").asText();
        if (addrNode.has("town")) return addrNode.get("town").asText();
        if (addrNode.has("village")) return addrNode.get("village").asText();
        if (addrNode.has("municipality")) return addrNode.get("municipality").asText();
        return null;
    }

    public Map<HealthUnit, Double> filterByRadius(List<HealthUnit> units, double baseLat, double baseLon, double radius, DistanceUnit unit) {
        Map<HealthUnit, Double> result = new HashMap<>();

        for (HealthUnit u : units) {
            if (u.getAddress().getLatitude() == null || u.getAddress().getLongitude() == null) continue;

            double distance = DistanceUtils.calculateDistance(
                baseLat, baseLon,
                u.getAddress().getLatitude(), u.getAddress().getLongitude(),
                unit
            );

            if (distance <= radius) {
                result.put(u, distance);
            }
        }
        return result;
    }

    public record GeocodedLocation(Double lat, Double lon, String city, String state) {}
}
