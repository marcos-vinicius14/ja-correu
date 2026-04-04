package org.jacorreu.onboarding.application.usecase;

import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.AvailableDaysPerWeek;
import org.jacorreu.onboarding.core.domain.valueobjects.CurrentPacePerKm;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAthleteProfileUseCaseTest {

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @InjectMocks
    private GetAthleteProfileUseCase getAthleteProfileUseCase;

    private final UUID profileId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private AthleteProfileDomain sampleProfile() {
        return AthleteProfileDomain.restore(
                profileId,
                userId,
                Goal.MARATHON,
                Level.ADVANCED,
                AvailableDaysPerWeek.create(5).getData(),
                CurrentPacePerKm.create("05:00").getData(),
                "Nenhuma lesao",
                false
        );
    }

    @Test
    void execute_profileExists_returnsSuccess() {
        when(athleteProfileRepository.findById(profileId)).thenReturn(Optional.of(sampleProfile()));

        Result<AthleteProfileResult> result = getAthleteProfileUseCase.execute(profileId);

        assertTrue(result.isSuccess());
        assertEquals(profileId, result.getData().id());
        assertEquals(userId, result.getData().userId());
        assertEquals(Goal.MARATHON, result.getData().goal());
        assertEquals(Level.ADVANCED, result.getData().level());
        assertEquals(5, result.getData().availableDaysPerWeek());
        assertEquals("05:00", result.getData().currentPacePerKm());
        assertEquals("Nenhuma lesao", result.getData().injuriesNotes());
    }

    @Test
    void execute_profileNotFound_returnsFailure() {
        when(athleteProfileRepository.findById(profileId)).thenReturn(Optional.empty());

        Result<AthleteProfileResult> result = getAthleteProfileUseCase.execute(profileId);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("id")));
    }
}
