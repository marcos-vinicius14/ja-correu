package org.jacorreu.onboarding.infra.handler;

import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.AvailableDaysPerWeek;
import org.jacorreu.onboarding.core.domain.valueobjects.CurrentPacePerKm;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.onboarding.core.gateway.OnboardingEmbeddingGateway;
import org.jacorreu.outbox.core.domain.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingEventHandlerTest {

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private OnboardingEmbeddingGateway embeddingGateway;

    @InjectMocks
    private OnboardingEventHandler eventHandler;

    private static final String EVENT_ONBOARDING_COMPLETED = "ONBOARDING_COMPLETED";

    @Test
    void should_returnTrue_whenCanHandleOnboardingCompletedEvent() {
        OutboxEvent event = OutboxEvent.create(
                UUID.randomUUID(),
                EVENT_ONBOARDING_COMPLETED,
                UUID.randomUUID().toString()
        );

        assertTrue(eventHandler.canHandle(event));
    }

    @Test
    void should_returnFalse_whenCannotHandleOtherEvents() {
        OutboxEvent event = OutboxEvent.create(
                UUID.randomUUID(),
                "OTHER_EVENT",
                UUID.randomUUID().toString()
        );

        assertFalse(eventHandler.canHandle(event));
    }

    @Test
    void should_deserializePayloadAndFindProfile_whenHandleCalled() {
        UUID profileId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(
                UUID.randomUUID(),
                EVENT_ONBOARDING_COMPLETED,
                profileId.toString()
        );

        AthleteProfileDomain profile = createProfile(profileId);
        when(athleteProfileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        eventHandler.handle(event);

        verify(athleteProfileRepository).findById(profileId);
    }

    @Test
    void should_callGenerateEmbedding_whenProfileFound() {
        UUID profileId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(
                UUID.randomUUID(),
                EVENT_ONBOARDING_COMPLETED,
                profileId.toString()
        );

        AthleteProfileDomain profile = createProfile(profileId);
        when(athleteProfileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        eventHandler.handle(event);

        verify(embeddingGateway).generateOnboardingEmbedding(profile);
    }

    @Test
    void should_throwException_whenProfileNotFound() {
        UUID profileId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(
                UUID.randomUUID(),
                EVENT_ONBOARDING_COMPLETED,
                profileId.toString()
        );

        when(athleteProfileRepository.findById(profileId))
                .thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            eventHandler.handle(event);
        });
    }

    @Test
    void should_notCallEmbeddingGateway_whenProfileNotFound() {
        UUID profileId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.create(
                UUID.randomUUID(),
                EVENT_ONBOARDING_COMPLETED,
                profileId.toString()
        );

        when(athleteProfileRepository.findById(profileId))
                .thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            eventHandler.handle(event);
        });

        verify(embeddingGateway, never()).generateOnboardingEmbedding(any());
    }

    private AthleteProfileDomain createProfile(UUID profileId) {
        return AthleteProfileDomain.restore(
                profileId,
                UUID.randomUUID(),
                Goal.FIVE_K,
                Level.BEGINNER,
                AvailableDaysPerWeek.restore(3),
                CurrentPacePerKm.restore(480),
                null,
                false
        );
    }
}
