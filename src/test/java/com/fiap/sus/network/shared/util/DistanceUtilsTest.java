package com.fiap.sus.network.shared.util;

import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceUtilsTest {

    @Test
    void calculateDistance_ShouldReturnZero_WhenCoordinatesAreSame() {
        double dist = DistanceUtils.calculateDistance(0.0, 0.0, 0.0, 0.0, DistanceUnit.KM);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void calculateDistance_ShouldReturnCorrectDistance_InKm() {
        // Distance between Krakow and Warsaw is approx 250-300km, let's use simpler points
        // (0,0) to (0,1) -> 1 degree latitude ~ 111km
        double dist = DistanceUtils.calculateDistance(0.0, 0.0, 1.0, 0.0, DistanceUnit.KM);
        assertEquals(111.0, dist, 2.0); // Approx 111.32 km
    }

    @Test
    void calculateDistance_ShouldReturnCorrectDistance_InMiles() {
        double dist = DistanceUtils.calculateDistance(0.0, 0.0, 1.0, 0.0, DistanceUnit.MILES);
        assertEquals(69.0, dist, 2.0); // Approx 69 miles
    }
    
    @Test
    void calculateBoundingBox_ShouldReturnArray() {
        double[] bbox = DistanceUtils.calculateBoundingBox(0.0, 0.0, 100.0);
        assertNotNull(bbox);
        assertEquals(4, bbox.length);
    }
}
