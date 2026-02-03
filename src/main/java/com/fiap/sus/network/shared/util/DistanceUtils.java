package com.fiap.sus.network.shared.util;

import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;

public class DistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_MILES = 3958.8;

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2, DistanceUnit unit) {
        double radius = switch (unit) {
            case KM, KILOMETERS -> EARTH_RADIUS_KM;
            case MILES -> EARTH_RADIUS_MILES;
            case METERS -> EARTH_RADIUS_KM * 1000.0;
        };

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius * c;
    }

    /**
     * Calculates a bounding box around a point.
     * Returns {minLat, maxLat, minLon, maxLon}
     */
    public static double[] calculateBoundingBox(double lat, double lon, double radiusKm) {
        // 1 degree of latitude is approximately 111 km
        double latChange = radiusKm / 111.0;
        
        // 1 degree of longitude depends on latitude
        double lonChange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return new double[] {
            lat - latChange,
            lat + latChange,
            lon - lonChange,
            lon + lonChange
        };
    }
}
