package org.jacorreu.onboarding.infra.web.dto;

import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;

import java.util.UUID;

public record OnboardingResponse(
        UUID id,
        UUID userId,
        Goal goal,
        Level level,
        int availableDaysPerWeek,
        String currentPacePerKm,
        String injuriesNotes
) {
    public static OnboardingResponse from(AthleteProfileResult result) {
        return new OnboardingResponse(
                result.id(),
                result.userId(),
                result.goal(),
                result.level(),
                result.availableDaysPerWeek(),
                result.currentPacePerKm(),
                result.injuriesNotes()
        );
    }
}
