package org.jacorreu.onboarding.core.domain;

import org.jacorreu.onboarding.core.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AthleteProfileDomainTest {

    private final UUID userId = UUID.randomUUID();
    private final Goal goal = Goal.HALF_MARATHON;
    private final Level level = Level.INTERMEDIATE;
    private final AvailableDaysPerWeek availableDays = AvailableDaysPerWeek.restore(4);
    private final CurrentPacePerKm pace = CurrentPacePerKm.restore(390);

    @Test
    void create_withValidData_succeeds() {
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId, goal, level, availableDays, pace, "Dor no joelho direito"
        );

        assertNotNull(profile.getId());
        assertEquals(userId, profile.getUserId());
        assertEquals(goal, profile.getGoal());
        assertEquals(level, profile.getLevel());
        assertEquals(availableDays, profile.getAvailableDaysPerWeek());
        assertEquals(pace, profile.getCurrentPacePerKm());
        assertEquals("Dor no joelho direito", profile.getInjuriesNotes());
    }

    @Test
    void create_withNullInjuriesNotes_succeeds() {
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId, goal, level, availableDays, pace, null
        );

        assertNotNull(profile.getId());
        assertNull(profile.getInjuriesNotes());
    }

    @Test
    void create_withBlankInjuriesNotes_succeeds() {
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId, goal, level, availableDays, pace, "  "
        );

        assertNotNull(profile.getId());
        assertNotNull(profile.getInjuriesNotes());
    }

    @Test
    void create_generatesUniqueId() {
        AthleteProfileDomain profile1 = AthleteProfileDomain.create(
                userId, goal, level, availableDays, pace, null
        );
        AthleteProfileDomain profile2 = AthleteProfileDomain.create(
                UUID.randomUUID(), goal, level, availableDays, pace, null
        );

        assertNotEquals(profile1.getId(), profile2.getId());
    }
}
