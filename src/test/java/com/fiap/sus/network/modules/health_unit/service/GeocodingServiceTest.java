package com.fiap.sus.network.modules.health_unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.sus.network.core.config.AppConfigProperties;
import com.fiap.sus.network.modules.health_unit.entity.Address;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AppConfigProperties appConfig;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GeocodingService service;

    private void mockAppConfig() {
        AppConfigProperties.Geocoding geocoding = new AppConfigProperties.Geocoding();
        geocoding.setUrl("http://api.test");
        when(appConfig.getGeocoding()).thenReturn(geocoding);
    }

    @Test
    void geocode_ShouldReturnLocation() {
        mockAppConfig();
        String jsonResponse = "[{\"lat\": \"-23.5505\", \"lon\": \"-46.6333\", \"address\": {\"city\": \"São Paulo\", \"state\": \"São Paulo\"}}]";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        GeocodingService.GeocodedLocation location = service.geocode("Praca da Se");

        assertEquals(-23.5505, location.lat());
        assertEquals(-46.6333, location.lon());
        assertEquals("São Paulo", location.city());
        assertEquals("São Paulo", location.state());
    }

    @Test
    void geocode_ShouldReturnEmpty_WhenNoResult() {
        mockAppConfig();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        GeocodingService.GeocodedLocation location = service.geocode("Unknown");

        assertEquals(0.0, location.lat());
        assertNull(location.city());
    }

    @Test
    void filterByRadius_ShouldReturnNearbyUnits() {
        HealthUnit unit1 = new HealthUnit();
        unit1.setAddress(new Address());
        unit1.getAddress().setLatitude(-23.5505);
        unit1.getAddress().setLongitude(-46.6333);

        HealthUnit unit2 = new HealthUnit();
        unit2.setAddress(new Address());
        unit2.getAddress().setLatitude(-23.6000);
        unit2.getAddress().setLongitude(-46.7000);

        List<HealthUnit> units = List.of(unit1, unit2);

        Map<HealthUnit, Double> result = service.filterByRadius(units, -23.5500, -46.6330, 2.0, DistanceUnit.KM);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(unit1));
    }
}
