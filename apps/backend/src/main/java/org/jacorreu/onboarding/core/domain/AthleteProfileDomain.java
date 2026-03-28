package org.jacorreu.onboarding.core.domain;

import com.fasterxml.uuid.Generators;
import org.jacorreu.onboarding.core.domain.valueobjects.*;

import java.util.Objects;
import java.util.UUID;

public final class AthleteProfileDomain {

    private final UUID id;
    private final UUID userId;
    private final Goal goal;
    private final Level level;
    private final AvailableDaysPerWeek availableDaysPerWeek;
    private final CurrentPacePerKm currentPacePerKm;
    private final String injuriesNotes;
    private final boolean embeddingGenerated;

    private AthleteProfileDomain(
            UUID id,
            UUID userId,
            Goal goal,
            Level level,
            AvailableDaysPerWeek availableDaysPerWeek,
            CurrentPacePerKm currentPacePerKm,
            String injuriesNotes,
            boolean embeddingGenerated
    ) {
        this.id = id;
        this.userId = userId;
        this.goal = goal;
        this.level = level;
        this.availableDaysPerWeek = availableDaysPerWeek;
        this.currentPacePerKm = currentPacePerKm;
        this.injuriesNotes = injuriesNotes;
        this.embeddingGenerated = embeddingGenerated;
    }

    public static AthleteProfileDomain create(
            UUID userId,
            Goal goal,
            Level level,
            AvailableDaysPerWeek availableDaysPerWeek,
            CurrentPacePerKm currentPacePerKm,
            String injuriesNotes
    ) {
        UUID id = Generators.timeBasedEpochGenerator().generate();
        return new AthleteProfileDomain(id, userId, goal, level, availableDaysPerWeek, currentPacePerKm, injuriesNotes, false);
    }

    public static AthleteProfileDomain restore(
            UUID id,
            UUID userId,
            Goal goal,
            Level level,
            AvailableDaysPerWeek availableDaysPerWeek,
            CurrentPacePerKm currentPacePerKm,
            String injuriesNotes,
            boolean embeddingGenerated
    ) {
        return new AthleteProfileDomain(id, userId, goal, level, availableDaysPerWeek, currentPacePerKm, injuriesNotes, embeddingGenerated);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Goal getGoal() {
        return goal;
    }

    public Level getLevel() {
        return level;
    }

    public AvailableDaysPerWeek getAvailableDaysPerWeek() {
        return availableDaysPerWeek;
    }

    public CurrentPacePerKm getCurrentPacePerKm() {
        return currentPacePerKm;
    }

    public String getInjuriesNotes() {
        return injuriesNotes;
    }

    public boolean isEmbeddingGenerated() {
        return embeddingGenerated;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AthleteProfileDomain that = (AthleteProfileDomain) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
