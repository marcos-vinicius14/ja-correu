package org.jacorreu.onboarding.infra.handler;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.onboarding.core.gateway.OnboardingEmbeddingGateway;
import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OnboardingEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OnboardingEventHandler.class);
    private static final String EVENT_ONBOARDING_COMPLETED = "ONBOARDING_COMPLETED";

    private final AthleteProfileRepository athleteProfileRepository;
    private final OnboardingEmbeddingGateway embeddingGateway;

    public OnboardingEventHandler(AthleteProfileRepository athleteProfileRepository,
            OnboardingEmbeddingGateway embeddingGateway) {
        this.athleteProfileRepository = athleteProfileRepository;
        this.embeddingGateway = embeddingGateway;
    }

    public boolean canHandle(OutboxEvent event) {
        return EVENT_ONBOARDING_COMPLETED.equals(event.eventType());
    }

    public void handle(OutboxEvent event) {
        UUID profileId = UUID.fromString(event.payload());

        log.info("Processing onboarding completion event for profile: {}", profileId);

        try {
            AthleteProfileDomain profile = athleteProfileRepository.findById(profileId)
                    .orElseThrow(() -> new EntityNotFoundException("Athlete profile not found: " + profileId));

            embeddingGateway.generateOnboardingEmbedding(profile);

            log.info("Successfully generated onboarding embedding for profile: {}", profileId);
        } catch (Exception e) {
            log.error("Failed to generate onboarding embedding for profile: {}", profileId, e);
            throw e;
        }
    }
}
