package com.fiap.sus.network.modules.health_unit.service;

import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fiap.sus.network.shared.util.DistanceUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    /**
     * Controla o tempo da última requisição ao Nominatim para garantir
     * o delay mínimo de 1 segundo entre requisições (política do Nominatim).
     */
    private static final java.util.concurrent.atomic.AtomicLong LAST_REQUEST_TIME = new java.util.concurrent.atomic.AtomicLong(0);
    private static final long MIN_DELAY_MS = 1000; // 1 segundo mínimo entre requisições

    public GeocodedLocation geocode(String address) {
        log.info("Fetching coordinates and location info for address: {}", address);
        
        enforceRateLimit();
        
        String normalizedAddress = normalizeAddress(address);
        log.debug("Normalized address: '{}' -> '{}'", address, normalizedAddress);
        
        String[] addressVariations = generateAddressVariations(normalizedAddress);
        
        for (int i = 0; i < addressVariations.length; i++) {
            String addressToTry = addressVariations[i];
            log.debug("Trying geocoding variation {}: '{}'", i + 1, addressToTry);
            
            try {
                GeocodedLocation result = tryGeocode(addressToTry);
                if (result != null && result.lat() != 0.0 && result.lon() != 0.0) {
                    log.info("Successfully geocoded address '{}' using variation {}: lat={}, lon={}", 
                            address, i + 1, result.lat(), result.lon());
                    return result;
                }
            } catch (Exception e) {
                log.debug("Geocoding variation {} failed: {}", i + 1, e.getMessage());
            }
        }
        
        log.warn("All geocoding attempts failed for address: {}. Using fallback coordinates.", address);
        GeocodedLocation fallback = getFallbackCoordinates(address);
        if (fallback != null) {
            log.info("Using fallback coordinates for address: {} -> lat={}, lon={}", address, fallback.lat(), fallback.lon());
            return fallback;
        }
        
        return new GeocodedLocation(0.0, 0.0, null, null);
    }
    
    /**
     * Tenta fazer geocoding de um endereço específico.
     */
    private GeocodedLocation tryGeocode(String address) {
        try {
            String baseUrl = appConfig.getGeocoding().getUrl();
            String encodedAddress = java.net.URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8);
            String url = baseUrl + "?q=" + encodedAddress + "&format=json&limit=1&addressdetails=1";
            log.debug("Geocoding URL: {}", url);
            
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                null, 
                String.class
            );
            
            String responseBody = response.getBody();
            log.debug("Geocoding response (first 200 chars): {}", 
                responseBody != null && responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);

            if (responseBody == null || responseBody.equals("[]") || responseBody.trim().isEmpty()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            
            if (!root.isArray() || root.isEmpty()) {
                return null;
            }
            
            JsonNode result = root.get(0);
            
            if (!result.has("lat") || !result.has("lon")) {
                return null;
            }
            
            double lat = result.get("lat").asDouble();
            double lon = result.get("lon").asDouble();
            
            log.debug("Geocoded coordinates: lat={}, lon={}", lat, lon);
            
            JsonNode addrNode = result.get("address");
            String city = extractCity(addrNode);
            String state = addrNode != null && addrNode.has("state") ? addrNode.get("state").asText() : null;

            return new GeocodedLocation(lat, lon, city, state);
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Rate limited by Nominatim. Waiting before retry...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            throw e;
        } catch (Exception e) {
            log.debug("Error in geocoding attempt: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Gera variações do endereço para tentar diferentes formatos.
     */
    private String[] generateAddressVariations(String address) {
        java.util.List<String> variations = new java.util.ArrayList<>();
        
        variations.add(address);
        
        String withoutNumber = address.replaceAll(",\\s*\\d+", "").trim();
        if (!withoutNumber.equals(address) && !variations.contains(withoutNumber)) {
            variations.add(withoutNumber);
        }
        
        String cityState = extractCityState(address);
        if (cityState != null && !cityState.isEmpty() && !variations.contains(cityState)) {
            variations.add(cityState);
        }
        
        String city = extractCityFromAddress(address);
        if (city != null && !city.isEmpty() && !variations.contains(city)) {
            variations.add(city);
        }
        
        return variations.toArray(new String[0]);
    }
    
    /**
     * Extrai cidade e estado do endereço (formato: "Cidade, Estado").
     */
    private String extractCityState(String address) {
        if (address == null) return null;
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*,\\s*([^,]+),\\s*([A-Z]{2})\\s*$");
        java.util.regex.Matcher matcher = pattern.matcher(address);
        
        if (matcher.find()) {
            String city = matcher.group(1).trim();
            String state = matcher.group(2).trim();
            return city + ", " + state;
        }
        
        return null;
    }
    
    /**
     * Extrai apenas a cidade do endereço.
     */
    private String extractCityFromAddress(String address) {
        if (address == null) return null;
        
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            String city = parts[parts.length - 2].trim();
            city = city.replaceFirst("^(São|Sao)\\s+", "");
            return city;
        }
        
        return null;
    }
    
    /**
     * Garante o delay mínimo de 1 segundo entre requisições ao Nominatim
     * para cumprir a política de uso do serviço.
     */
    private void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long lastRequest = LAST_REQUEST_TIME.get();
        long timeSinceLastRequest = now - lastRequest;
        
        if (timeSinceLastRequest < MIN_DELAY_MS) {
            long delayNeeded = MIN_DELAY_MS - timeSinceLastRequest;
            log.debug("Rate limiting: waiting {}ms before next Nominatim request", delayNeeded);
            try {
                Thread.sleep(delayNeeded);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limit delay interrupted");
            }
        }
        
        LAST_REQUEST_TIME.set(System.currentTimeMillis());
    }

    /**
     * Normaliza o endereço antes de enviar ao Nominatim.
     * Remove vírgulas extras, normaliza espaços e simplifica o formato.
     */
    private String normalizeAddress(String address) {
        if (address == null || address.isBlank()) {
            return address;
        }
        
        return address
            .replaceAll(",\\s*,", ",")
            .replaceAll("\\s+", " ")
            .replaceAll("^\\s+|\\s+$", "")
            .trim();
    }

    /**
     * Retorna coordenadas conhecidas para endereços comuns quando o Nominatim falha.
     */
    private GeocodedLocation getFallbackCoordinates(String address) {
        if (address == null) return null;
        
        String normalized = address.toLowerCase().trim();
        
        if (normalized.contains("são paulo") || normalized.contains("sao paulo") || normalized.contains("paulista")) {
            return new GeocodedLocation(-23.5505, -46.6333, "São Paulo", "SP");
        }
        if (normalized.contains("rio de janeiro") || normalized.contains("rio")) {
            return new GeocodedLocation(-22.9068, -43.1729, "Rio de Janeiro", "RJ");
        }
        if (normalized.contains("belo horizonte") || normalized.contains("bh")) {
            return new GeocodedLocation(-19.9167, -43.9345, "Belo Horizonte", "MG");
        }
        if (normalized.contains("brasília") || normalized.contains("brasilia")) {
            return new GeocodedLocation(-15.7942, -47.8822, "Brasília", "DF");
        }
        if (normalized.contains("curitiba")) {
            return new GeocodedLocation(-25.4284, -49.2733, "Curitiba", "PR");
        }
        if (normalized.contains("porto alegre")) {
            return new GeocodedLocation(-30.0346, -51.2177, "Porto Alegre", "RS");
        }
        if (normalized.contains("salvador")) {
            return new GeocodedLocation(-12.9714, -38.5014, "Salvador", "BA");
        }
        if (normalized.contains("recife")) {
            return new GeocodedLocation(-8.0476, -34.8770, "Recife", "PE");
        }
        if (normalized.contains("fortaleza")) {
            return new GeocodedLocation(-3.7172, -38.5433, "Fortaleza", "CE");
        }
        if (normalized.contains("manaus")) {
            return new GeocodedLocation(-3.1190, -60.0217, "Manaus", "AM");
        }
        
        return null;
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
        Map<HealthUnit, Double> result = new java.util.LinkedHashMap<>();

        List<HealthUnit> unitsCopy = new java.util.ArrayList<>(units);
        
        for (HealthUnit u : unitsCopy) {
            if (u == null || u.getAddress() == null) continue;
            
            Double lat = u.getAddress().getLatitude();
            Double lon = u.getAddress().getLongitude();
            
            if (lat == null || lon == null) continue;

            double distance = DistanceUtils.calculateDistance(
                baseLat, baseLon,
                lat, lon,
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
