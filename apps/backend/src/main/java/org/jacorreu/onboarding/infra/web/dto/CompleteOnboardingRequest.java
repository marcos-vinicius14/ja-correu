package org.jacorreu.onboarding.infra.web.dto;

import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;

public record CompleteOnboardingRequest(
        Goal goal,
        Level level,
        int availableDaysPerWeek,
        String currentPacePerKm,
        String injuriesNotes
) {}
