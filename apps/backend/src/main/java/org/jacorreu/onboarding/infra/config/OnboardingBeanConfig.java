package org.jacorreu.onboarding.infra.config;

import org.jacorreu.onboarding.application.usecase.CompleteOnboardingUseCase;
import org.jacorreu.onboarding.application.usecase.GetAthleteProfileUseCase;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.onboarding.core.gateway.EventPublisher;
import org.jacorreu.user.core.gateway.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OnboardingBeanConfig {

    @Bean
    public CompleteOnboardingUseCase completeOnboardingUseCase(
            UserRepository userRepository,
            AthleteProfileRepository athleteProfileRepository,
            EventPublisher eventPublisher
    ) {
        return new CompleteOnboardingUseCase(userRepository, athleteProfileRepository, eventPublisher);
    }

    @Bean
    public GetAthleteProfileUseCase getAthleteProfileUseCase(AthleteProfileRepository athleteProfileRepository) {
        return new GetAthleteProfileUseCase(athleteProfileRepository);
    }
}
