package org.jacorreu.onboarding.core.domain.valueobjects;

public enum Goal {
    FIVE_K(5.0),
    TEN_K(10.0),
    HALF_MARATHON(21.097),
    MARATHON(42.195);

    private final double targetDistanceKm;

    Goal(double targetDistanceKm) {
        this.targetDistanceKm = targetDistanceKm;
    }

    public double targetDistanceKm() {
        return targetDistanceKm;
    }
}
