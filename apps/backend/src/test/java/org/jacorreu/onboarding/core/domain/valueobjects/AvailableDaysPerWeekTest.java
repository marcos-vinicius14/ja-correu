package org.jacorreu.onboarding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvailableDaysPerWeekTest {

    @Test
    void create_withMinimumValue_succeeds() {
        Result<AvailableDaysPerWeek> result = AvailableDaysPerWeek.create(2);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().getValue());
    }

    @Test
    void create_withMaximumValue_succeeds() {
        Result<AvailableDaysPerWeek> result = AvailableDaysPerWeek.create(7);
        assertTrue(result.isSuccess());
        assertEquals(7, result.getData().getValue());
    }

    @Test
    void create_withMiddleValue_succeeds() {
        Result<AvailableDaysPerWeek> result = AvailableDaysPerWeek.create(4);
        assertTrue(result.isSuccess());
    }

    @Test
    void create_belowMinimum_fails() {
        Result<AvailableDaysPerWeek> result = AvailableDaysPerWeek.create(1);
        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
    }

    @Test
    void create_aboveMaximum_fails() {
        Result<AvailableDaysPerWeek> result = AvailableDaysPerWeek.create(8);
        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
    }

    @Test
    void restore_returnsWithoutValidation() {
        AvailableDaysPerWeek vo = AvailableDaysPerWeek.restore(5);
        assertEquals(5, vo.getValue());
    }
}
