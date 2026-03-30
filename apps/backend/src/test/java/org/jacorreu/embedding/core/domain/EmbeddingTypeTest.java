package org.jacorreu.embedding.core.domain;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddingTypeTest {

    @Test
    void should_containOnboardingValue() {
        assertTrue(EnumSet.allOf(EmbeddingType.class).contains(EmbeddingType.ONBOARDING));
    }

    @Test
    void should_containWorkoutValue() {
        assertTrue(EnumSet.allOf(EmbeddingType.class).contains(EmbeddingType.WORKOUT));
    }

    @Test
    void should_containWeeklySummaryValue() {
        assertTrue(EnumSet.allOf(EmbeddingType.class).contains(EmbeddingType.WEEKLY_SUMMARY));
    }

    @Test
    void should_haveExactlyThreeValues() {
        assertEquals(3, EmbeddingType.values().length);
    }
}
