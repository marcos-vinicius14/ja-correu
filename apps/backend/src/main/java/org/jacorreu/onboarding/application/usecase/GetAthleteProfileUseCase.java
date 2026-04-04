package org.jacorreu.onboarding.application.usecase;

import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

import java.util.UUID;

public final class GetAthleteProfileUseCase {

    private final AthleteProfileRepository athleteProfileRepository;

    public GetAthleteProfileUseCase(AthleteProfileRepository athleteProfileRepository) {
        this.athleteProfileRepository = athleteProfileRepository;
    }

    public Result<AthleteProfileResult> execute(UUID id) {
        var notification = new Notification();

        return athleteProfileRepository.findById(id)
                .<Result<AthleteProfileResult>>map(profile -> Result.success(new AthleteProfileResult(
                        profile.getId(),
                        profile.getUserId(),
                        profile.getGoal(),
                        profile.getLevel(),
                        profile.getAvailableDaysPerWeek().getValue(),
                        profile.getCurrentPacePerKm().toFormattedString(),
                        profile.getInjuriesNotes())))
                .orElseGet(() -> {
                    notification.addError("id", "Perfil de atleta nao encontrado");
                    return Result.failure(notification);
                });
    }
}
