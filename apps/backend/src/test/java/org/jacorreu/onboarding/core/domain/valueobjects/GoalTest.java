package org.jacorreu.onboarding.core.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoalTest {

    @Test
    void fiveK_targetDistanceIs5km() {
        assertEquals(5.0, Goal.FIVE_K.targetDistanceKm());
    }

    @Test
    void tenK_targetDistanceIs10km() {
        assertEquals(10.0, Goal.TEN_K.targetDistanceKm());
    }

    @Test
    void halfMarathon_targetDistanceIs21point097km() {
        assertEquals(21.097, Goal.HALF_MARATHON.targetDistanceKm());
    }

    @Test
    void marathon_targetDistanceIs42point195km() {
        assertEquals(42.195, Goal.MARATHON.targetDistanceKm());
    }
}
