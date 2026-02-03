package com.fiap.sus.network.modules.health_unit.enums;

public enum DistanceUnit {
    KM,
    KILOMETERS,
    METERS,
    MILES;

    public boolean isKmEquivalent() {
        return this == KM || this == KILOMETERS;
    }

    public double toKilometers(double value) {
        return switch (this) {
            case KM, KILOMETERS -> value;
            case METERS -> value / 1000.0;
            case MILES -> value * 1.60934;
        };
    }
}
