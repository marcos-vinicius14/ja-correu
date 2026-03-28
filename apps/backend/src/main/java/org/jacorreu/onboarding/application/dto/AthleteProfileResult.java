package org.jacorreu.onboarding.application.dto;

import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;

import java.util.UUID;

public record AthleteProfileResult(
        UUID id,
        UUID userId,
        Goal goal,
        Level level,
        int availableDaysPerWeek,
        String currentPacePerKm,
        String injuriesNotes
) {}
