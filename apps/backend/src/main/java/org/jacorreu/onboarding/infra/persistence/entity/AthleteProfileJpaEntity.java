package org.jacorreu.onboarding.infra.persistence.entity;

import jakarta.persistence.*;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_athlete_profiles")
public class AthleteProfileJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "goal", columnDefinition = "athlete_goal")
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "experience_level", columnDefinition = "experience_level")
    private Level level;

    @Column(nullable = false, name = "available_days_per_week")
    private int availableDaysPerWeek;

    @Column(nullable = false, name = "current_pace_per_km", columnDefinition = "integer")
    private int currentPacePerKmSeconds;

    @Column(name = "injuries_notes")
    private String injuriesNotes;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @Column(nullable = false, name = "embedding_generated")
    private boolean embeddingGenerated;

    protected AthleteProfileJpaEntity() {
    }

    public AthleteProfileJpaEntity(
            UUID id,
            UUID userId,
            Goal goal,
            Level level,
            int availableDaysPerWeek,
            int currentPacePerKmSeconds,
            String injuriesNotes,
            boolean embeddingGenerated
    ) {
        this.id = id;
        this.userId = userId;
        this.goal = goal;
        this.level = level;
        this.availableDaysPerWeek = availableDaysPerWeek;
        this.currentPacePerKmSeconds = currentPacePerKmSeconds;
        this.injuriesNotes = injuriesNotes;
        this.embeddingGenerated = embeddingGenerated;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Goal getGoal() { return goal; }
    public Level getLevel() { return level; }
    public int getAvailableDaysPerWeek() { return availableDaysPerWeek; }
    public int getCurrentPacePerKmSeconds() { return currentPacePerKmSeconds; }
    public String getInjuriesNotes() { return injuriesNotes; }
    public boolean isEmbeddingGenerated() { return embeddingGenerated; }
}
