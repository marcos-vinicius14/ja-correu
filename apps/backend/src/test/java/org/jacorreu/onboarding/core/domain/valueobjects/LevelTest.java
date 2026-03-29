package org.jacorreu.onboarding.core.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelTest {

    @Test
    void beginner_maxWeeklyVolumeIs30km() {
        assertEquals(30.0, Level.BEGINNER.maxWeeklyVolumeKm());
    }

    @Test
    void intermediate_maxWeeklyVolumeIs60km() {
        assertEquals(60.0, Level.INTERMEDIATE.maxWeeklyVolumeKm());
    }

    @Test
    void advanced_maxWeeklyVolumeIs100km() {
        assertEquals(100.0, Level.ADVANCED.maxWeeklyVolumeKm());
    }
}
