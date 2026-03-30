package org.jacorreu.onboarding.infra.ai;

import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.gateway.OnboardingEmbeddingGateway;
import org.springframework.stereotype.Component;

@Component
public class OnboardingEmbeddingGatewayImpl implements OnboardingEmbeddingGateway {

    private final EmbeddingGateway embeddingGateway;

    public OnboardingEmbeddingGatewayImpl(EmbeddingGateway embeddingGateway) {
        this.embeddingGateway = embeddingGateway;
    }

    @Override
    public void generateOnboardingEmbedding(AthleteProfileDomain profile) {
        String content = buildContextText(profile);
        var userId = EmbeddingUserId.restore(profile.getUserId());
        var embeddingContent = EmbeddingContent.restore(content);
        embeddingGateway.save(userId, EmbeddingType.ONBOARDING, embeddingContent);
    }

    private String buildContextText(AthleteProfileDomain profile) {
        String injuries = profile.getInjuriesNotes() == null || profile.getInjuriesNotes().isBlank()
                ? "nenhuma"
                : profile.getInjuriesNotes();

        return """
                Atleta com objetivo de completar %s.
                Nível de experiência: %s.
                Disponibilidade de %d dias por semana para treinar.
                Pace atual: %s por km.
                Restrições e lesões: %s."""
                .formatted(
                        formatGoal(profile.getGoal()),
                        profile.getLevel().name(),
                        profile.getAvailableDaysPerWeek().getValue(),
                        profile.getCurrentPacePerKm().toFormattedString(),
                        injuries
                );
    }

    private String formatGoal(Goal goal) {
        return switch (goal) {
            case FIVE_K -> "5km";
            case TEN_K -> "10km";
            case HALF_MARATHON -> "21.097km (meia maratona)";
            case MARATHON -> "42.195km (maratona)";
        };
    }
}
