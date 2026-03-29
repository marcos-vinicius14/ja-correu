package org.jacorreu.onboarding.infra.persistence.mapper;

import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.AvailableDaysPerWeek;
import org.jacorreu.onboarding.core.domain.valueobjects.CurrentPacePerKm;
import org.jacorreu.onboarding.infra.persistence.entity.AthleteProfileJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AthleteProfileMapper {

    public AthleteProfileDomain toDomain(AthleteProfileJpaEntity entity) {
        return AthleteProfileDomain.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getGoal(),
                entity.getLevel(),
                AvailableDaysPerWeek.restore(entity.getAvailableDaysPerWeek()),
                CurrentPacePerKm.restore(entity.getCurrentPacePerKmSeconds()),
                entity.getInjuriesNotes(),
                entity.isEmbeddingGenerated()
        );
    }

    public AthleteProfileJpaEntity toEntity(AthleteProfileDomain domain) {
        return new AthleteProfileJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getGoal(),
                domain.getLevel(),
                domain.getAvailableDaysPerWeek().getValue(),
                domain.getCurrentPacePerKm().toSeconds(),
                domain.getInjuriesNotes(),
                domain.isEmbeddingGenerated()
        );
    }
}
