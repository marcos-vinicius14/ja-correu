package org.jacorreu.onboarding.core.domain.valueobjects;

public enum Level {
    BEGINNER(30.0),
    INTERMEDIATE(60.0),
    ADVANCED(100.0);

    private final double maxWeeklyVolumeKm;

    Level(double maxWeeklyVolumeKm) {
        this.maxWeeklyVolumeKm = maxWeeklyVolumeKm;
    }

    public double maxWeeklyVolumeKm() {
        return maxWeeklyVolumeKm;
    }
}
