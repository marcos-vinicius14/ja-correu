package org.jacorreu.onboarding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrentPacePerKmTest {

    @Test
    void create_withLowerBoundary_succeeds() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create("03:00");
        assertTrue(result.isSuccess());
        assertEquals(180, result.getData().toSeconds());
    }

    @Test
    void create_withUpperBoundary_succeeds() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create("15:00");
        assertTrue(result.isSuccess());
        assertEquals(900, result.getData().toSeconds());
    }

    @Test
    void create_withTypicalPace_succeeds() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create("06:30");
        assertTrue(result.isSuccess());
        assertEquals("06:30", result.getData().toFormattedString());
        assertEquals(390, result.getData().toSeconds());
    }

    @Test
    void create_belowLowerBoundary_fails() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create("02:59");
        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
    }

    @Test
    void create_aboveUpperBoundary_fails() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create("15:01");
        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
    }

    @Test
    void create_withInvalidFormat_fails() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create("abc");
        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
    }

    @Test
    void create_withNull_fails() {
        Result<CurrentPacePerKm> result = CurrentPacePerKm.create(null);
        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
    }

    @Test
    void restore_fromSeconds_roundTripsCorrectly() {
        CurrentPacePerKm vo = CurrentPacePerKm.restore(390);
        assertEquals("06:30", vo.toFormattedString());
        assertEquals(390, vo.toSeconds());
    }
}
