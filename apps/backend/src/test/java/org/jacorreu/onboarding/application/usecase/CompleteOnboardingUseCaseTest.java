package org.jacorreu.onboarding.application.usecase;

import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.application.dto.CompleteOnboardingCommand;
import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.onboarding.core.gateway.EventPublisher;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.UserStatus;
import org.jacorreu.user.core.gateway.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteOnboardingUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AthleteProfileRepository athleteProfileRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CompleteOnboardingUseCase completeOnboardingUseCase;

    private final UUID userId = UUID.randomUUID();

    private UserDomain pendingUser() {
        return UserDomain.restore(
                userId, "Atleta Teste",
                "atleta@example.com", "encodedPassword",
                null, null, null,
                UserStatus.PENDING_ONBOARDING
        );
    }

    private UserDomain activeUser() {
        return UserDomain.restore(
                userId, "Atleta Teste",
                "atleta@example.com", "encodedPassword",
                null, null, null,
                UserStatus.ACTIVE
        );
    }

    private CompleteOnboardingCommand validCommand() {
        return new CompleteOnboardingCommand(
                userId,
                Goal.HALF_MARATHON,
                Level.INTERMEDIATE,
                4,
                "06:30",
                "Dor no joelho direito"
        );
    }

    @Test
    void execute_happyPath_savesProfileActivatesUserAndTriggersEmbedding() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser()));

        Result<AthleteProfileResult> result = completeOnboardingUseCase.execute(validCommand());

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(userId, result.getData().userId());
        assertEquals(Goal.HALF_MARATHON, result.getData().goal());
        assertEquals(Level.INTERMEDIATE, result.getData().level());
        assertEquals(4, result.getData().availableDaysPerWeek());
        assertEquals("06:30", result.getData().currentPacePerKm());
        assertEquals("Dor no joelho direito", result.getData().injuriesNotes());

        verify(athleteProfileRepository, times(1)).save(any(AthleteProfileDomain.class));

        ArgumentCaptor<UserDomain> userCaptor = ArgumentCaptor.forClass(UserDomain.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());

        verify(eventPublisher, times(1)).publish(any(), eq("ONBOARDING_COMPLETED"), any());
    }

    @Test
    void execute_userNotFound_returnsFailure() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Result<AthleteProfileResult> result = completeOnboardingUseCase.execute(validCommand());

        assertFalse(result.isSuccess());
        assertFalse(result.getNotification().getErrors().isEmpty());
        verify(athleteProfileRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void execute_userAlreadyActive_returnsFailureWithAlreadyActiveError() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser()));

        Result<AthleteProfileResult> result = completeOnboardingUseCase.execute(validCommand());

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("already_active")));
        verify(athleteProfileRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void execute_invalidAvailableDays_returnsValidationFailure() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser()));

        CompleteOnboardingCommand invalidCommand = new CompleteOnboardingCommand(
                userId,
                Goal.HALF_MARATHON,
                Level.INTERMEDIATE,
                1,       // inválido: abaixo do mínimo de 2
                "06:30",
                null
        );

        Result<AthleteProfileResult> result = completeOnboardingUseCase.execute(invalidCommand);

        assertFalse(result.isSuccess());
        verify(athleteProfileRepository, never()).save(any());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void execute_invalidPaceFormat_returnsValidationFailure() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser()));

        CompleteOnboardingCommand invalidCommand = new CompleteOnboardingCommand(
                userId,
                Goal.HALF_MARATHON,
                Level.INTERMEDIATE,
                4,
                "invalid-pace",
                null
        );

        Result<AthleteProfileResult> result = completeOnboardingUseCase.execute(invalidCommand);

        assertFalse(result.isSuccess());
        verify(athleteProfileRepository, never()).save(any());
    }
}
