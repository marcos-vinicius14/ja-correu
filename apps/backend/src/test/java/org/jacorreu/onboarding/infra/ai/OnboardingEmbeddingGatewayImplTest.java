package org.jacorreu.onboarding.infra.ai;

import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.AvailableDaysPerWeek;
import org.jacorreu.onboarding.core.domain.valueobjects.CurrentPacePerKm;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;
import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingEmbeddingGatewayImplTest {

    @Mock
    private EmbeddingGateway embeddingGateway;

    @InjectMocks
    private OnboardingEmbeddingGatewayImpl onboardingGateway;

    @BeforeEach
    void setUp() {
        when(embeddingGateway.save(any(), any(), any())).thenReturn(Result.success());
    }

    @Test
    void should_generateCorrectContextText_whenProfileProvided() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId,
                Goal.MARATHON,
                Level.ADVANCED,
                AvailableDaysPerWeek.create(5).getData(),
                CurrentPacePerKm.create("05:30").getData(),
                "Dor no joelho esquerdo"
        );

        onboardingGateway.generateOnboardingEmbedding(profile);

        ArgumentCaptor<EmbeddingContent> contentCaptor = ArgumentCaptor.forClass(EmbeddingContent.class);
        verify(embeddingGateway).save(
                any(EmbeddingUserId.class),
                eq(EmbeddingType.ONBOARDING),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue().getValue();
        assertTrue(content.contains("objetivo de completar"));
        assertTrue(content.contains("42.195"));
        assertTrue(content.contains("Nível de experiência: ADVANCED"));
        assertTrue(content.contains("5 dias por semana"));
        assertTrue(content.contains("05:30 por km"));
        assertTrue(content.contains("Dor no joelho esquerdo"));
    }

    @Test
    void should_callEmbeddingGatewaySave_withOnboardingType() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = createMinimalProfile(userId);

        onboardingGateway.generateOnboardingEmbedding(profile);

        verify(embeddingGateway).save(
                any(EmbeddingUserId.class),
                eq(EmbeddingType.ONBOARDING),
                any(EmbeddingContent.class)
        );
    }

    @Test
    void should_handleNullInjuriesNotes_asNenhuma() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId,
                Goal.FIVE_K,
                Level.BEGINNER,
                AvailableDaysPerWeek.create(3).getData(),
                CurrentPacePerKm.create("08:00").getData(),
                null
        );

        onboardingGateway.generateOnboardingEmbedding(profile);

        ArgumentCaptor<EmbeddingContent> contentCaptor = ArgumentCaptor.forClass(EmbeddingContent.class);
        verify(embeddingGateway).save(any(), any(), contentCaptor.capture());

        assertTrue(contentCaptor.getValue().getValue().contains("nenhuma"));
    }

    @Test
    void should_useCorrectUserId_fromProfile() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = createMinimalProfile(userId);

        onboardingGateway.generateOnboardingEmbedding(profile);

        ArgumentCaptor<EmbeddingUserId> userIdCaptor = ArgumentCaptor.forClass(EmbeddingUserId.class);
        verify(embeddingGateway).save(userIdCaptor.capture(), any(), any());
        assertEquals(userId, userIdCaptor.getValue().getValue());
    }

    @Test
    void should_formatFiveKGoalCorrectly() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId,
                Goal.FIVE_K,
                Level.BEGINNER,
                AvailableDaysPerWeek.create(3).getData(),
                CurrentPacePerKm.create("08:00").getData(),
                null
        );

        onboardingGateway.generateOnboardingEmbedding(profile);

        ArgumentCaptor<EmbeddingContent> contentCaptor = ArgumentCaptor.forClass(EmbeddingContent.class);
        verify(embeddingGateway).save(any(), any(), contentCaptor.capture());

        assertTrue(contentCaptor.getValue().getValue().contains("5km"));
    }

    @Test
    void should_formatTenKGoalCorrectly() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId,
                Goal.TEN_K,
                Level.INTERMEDIATE,
                AvailableDaysPerWeek.create(4).getData(),
                CurrentPacePerKm.create("06:30").getData(),
                null
        );

        onboardingGateway.generateOnboardingEmbedding(profile);

        ArgumentCaptor<EmbeddingContent> contentCaptor = ArgumentCaptor.forClass(EmbeddingContent.class);
        verify(embeddingGateway).save(any(), any(), contentCaptor.capture());

        assertTrue(contentCaptor.getValue().getValue().contains("10km"));
    }

    @Test
    void should_formatHalfMarathonGoalCorrectly() {
        UUID userId = UUID.randomUUID();
        AthleteProfileDomain profile = AthleteProfileDomain.create(
                userId,
                Goal.HALF_MARATHON,
                Level.ADVANCED,
                AvailableDaysPerWeek.create(5).getData(),
                CurrentPacePerKm.create("05:30").getData(),
                null
        );

        onboardingGateway.generateOnboardingEmbedding(profile);

        ArgumentCaptor<EmbeddingContent> contentCaptor = ArgumentCaptor.forClass(EmbeddingContent.class);
        verify(embeddingGateway).save(any(), any(), contentCaptor.capture());

        String content = contentCaptor.getValue().getValue();
        assertTrue(content.contains("21.097"));
    }

    private AthleteProfileDomain createMinimalProfile(UUID userId) {
        return AthleteProfileDomain.create(
                userId,
                Goal.FIVE_K,
                Level.BEGINNER,
                AvailableDaysPerWeek.create(3).getData(),
                CurrentPacePerKm.create("08:00").getData(),
                null
        );
    }
}
