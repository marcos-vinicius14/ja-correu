package org.jacorreu.onboarding.application.usecase;

import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.application.dto.CompleteOnboardingCommand;
import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.AvailableDaysPerWeek;
import org.jacorreu.onboarding.core.domain.valueobjects.CurrentPacePerKm;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.onboarding.core.gateway.EventPublisher;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.UserStatus;
import org.jacorreu.user.core.gateway.UserRepository;

public final class CompleteOnboardingUseCase {

    private static final String EVENT_ONBOARDING_COMPLETED = "ONBOARDING_COMPLETED";

    private final UserRepository userRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final EventPublisher eventPublisher;

    public CompleteOnboardingUseCase(
            UserRepository userRepository,
            AthleteProfileRepository athleteProfileRepository,
            EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.athleteProfileRepository = athleteProfileRepository;
        this.eventPublisher = eventPublisher;
    }

    public Result<AthleteProfileResult> execute(CompleteOnboardingCommand command) {
        var notification = new Notification();

        return userRepository.findById(command.userId())
                .<Result<AthleteProfileResult>>map(user -> validateUserStatus(user, command, notification))
                .orElseGet(() -> {
                    notification.addError("userId", "Usuario nao encontrado");
                    return Result.failure(notification);
                });
    }

    private Result<AthleteProfileResult> validateUserStatus(UserDomain user,
            CompleteOnboardingCommand command, Notification notification) {

        return switch (user.getStatus()) {
            case UserStatus.PENDING_ONBOARDING -> validateAndCreateProfile(user, command, notification);
            case UserStatus.ACTIVE -> {
                notification.addError("already_active", "Onboarding ja foi concluido para este usuario");
                yield Result.failure(notification);
            }
            default -> {
                notification.addError("invalid_status", "Status de usuario invalido para completar onboarding");
                yield Result.failure(notification);
            }
        };
    }

    private Result<AthleteProfileResult> validateAndCreateProfile(UserDomain user,
            CompleteOnboardingCommand command, Notification notification) {

        notification.merge(AvailableDaysPerWeek.create(command.availableDaysPerWeek()));
        notification.merge(CurrentPacePerKm.create(command.currentPacePerKm()));

        return notification.hasErrors()
                ? Result.failure(notification)
                : createProfileAndActivateUser(user, command,
                        AvailableDaysPerWeek.create(command.availableDaysPerWeek()).getData(),
                        CurrentPacePerKm.create(command.currentPacePerKm()).getData());
    }

    private Result<AthleteProfileResult> createProfileAndActivateUser(UserDomain user,
            CompleteOnboardingCommand command, AvailableDaysPerWeek days, CurrentPacePerKm pace) {

        var profile = AthleteProfileDomain.create(
                command.userId(),
                command.goal(),
                command.level(),
                days,
                pace,
                command.injuriesNotes());

        athleteProfileRepository.save(profile);

        userRepository.save(user.activate());

        var payload = """
            {
                "profileId": "%s",
                "userId": "%s",
                "goal": "%s",
                "level": "%s"
            }
            """.formatted(
                profile.getId(),
                profile.getUserId(),
                profile.getGoal(),
                profile.getLevel()
            );

        eventPublisher.publish(profile.getId(), EVENT_ONBOARDING_COMPLETED, payload);

        return Result.success(buildResult(profile));
    }

    private AthleteProfileResult buildResult(AthleteProfileDomain profile) {
        return new AthleteProfileResult(
                profile.getId(),
                profile.getUserId(),
                profile.getGoal(),
                profile.getLevel(),
                profile.getAvailableDaysPerWeek().getValue(),
                profile.getCurrentPacePerKm().toFormattedString(),
                profile.getInjuriesNotes());
    }
}
