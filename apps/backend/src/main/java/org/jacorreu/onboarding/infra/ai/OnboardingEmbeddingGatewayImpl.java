package org.jacorreu.onboarding.infra.ai;

import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.gateway.OnboardingEmbeddingGateway;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OnboardingEmbeddingGatewayImpl implements OnboardingEmbeddingGateway {

    private final VectorStore vectorStore;

    public OnboardingEmbeddingGatewayImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void generateOnboardingEmbedding(AthleteProfileDomain profile) {
        String content = buildProfileText(profile);

        Document document = new Document(
                content,
                Map.of(
                        "userId", profile.getUserId().toString(),
                        "goal", profile.getGoal().name(),
                        "level", profile.getLevel().name(),
                        "currentPacePerKm", profile.getCurrentPacePerKm().toFormattedString(),
                        "availableDaysPerWeek", String.valueOf(profile.getAvailableDaysPerWeek().getValue())
                )
        );

        vectorStore.add(List.of(document));
    }

    private String buildProfileText(AthleteProfileDomain profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Atleta com objetivo de correr ").append(profile.getGoal().targetDistanceKm()).append(" km. ");
        sb.append("Nível: ").append(profile.getLevel().name()).append(". ");
        sb.append("Volume máximo semanal recomendado: ").append(profile.getLevel().maxWeeklyVolumeKm()).append(" km. ");
        sb.append("Disponibilidade: ").append(profile.getAvailableDaysPerWeek().getValue()).append(" dias por semana. ");
        sb.append("Pace atual: ").append(profile.getCurrentPacePerKm().toFormattedString()).append(" min/km. ");
        if (profile.getInjuriesNotes() != null && !profile.getInjuriesNotes().isBlank()) {
            sb.append("Histórico de lesões: ").append(profile.getInjuriesNotes()).append(".");
        }
        return sb.toString();
    }
}
